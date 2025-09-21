import http from 'k6/http';
import { check, sleep } from 'k6';

let messageCounters = {};

export let options = {
	scenarios: {
		light: {
			executor: 'constant-vus',
			vus: 10,
			duration: '10s',
		},
		medium: {
			executor: 'ramping-vus',
			startVUs: 0,
			stages: [
				{ duration: '10s', target: 50 },
				{ duration: '20s', target: 50 },
				{ duration: '10s', target: 0 },
			],
		},
		heavy: {
			executor: 'constant-arrival-rate',
			rate: 100,
			timeUnit: '1s',
			duration: '30s',
			preAllocatedVUs: 50,
			maxVUs: 200,
		},
	},
	thresholds: {
		http_req_duration: ['p(95)<200', 'p(99)<500'], // SLA
		http_req_failed: ['rate<0.01'], // <1% erorrrs
	},
};

export default function () {
	let url = 'http://localhost:8088/messages';
	let channel = `rocket-${__VU}`;

	if (!messageCounters[channel]) {
		messageCounters[channel] = 1;
	} else {
		messageCounters[channel]++;
	}

	let payload = JSON.stringify({
		metadata: {
			channel: channel,
			messageNumber: messageCounters[channel],
			messageTime: new Date().toISOString(),
			messageType: messageCounters[channel] === 1
				? 'RocketLaunched'
				: 'RocketSpeedIncreased',
		},
		message: messageCounters[channel] === 1
			? {
				type: 'Falcon-9',
				launchSpeed: 1000,
				mission: 'LOAD-TEST',
			}
			: {
				by: 500 + Math.floor(Math.random() * 500),
			},
	});

	let params = {
		headers: { 'Content-Type': 'application/json' },
	};

	let res = http.post(url, payload, params);

	check(res, {
		'status is 200': (r) => r.status === 200,
	});

	sleep(0.2);
}