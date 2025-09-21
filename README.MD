# Lunar Rockets API & Dashboard

This project simulates rocket telemetry processing and visualization.  
It consists of two parts:

- **Backend (Spring Boot + Redis/InMemory)** — processes telemetry events and provides REST APIs.
- **Simple dashboard (React/Vite)** — displays rocket states and history.

---

## Architecture
![Diagram](src/main/resources/static/Diagram.png)

---
## Domain-Driven Design (DDD)
I modeled the rocket system with root and events:

- Root → `RocketTracker`
- Entities -> rocket state (`RocketStatus`)
- Events (`RocketEvent`) → immutable descriptions of state changes:
    - `RocketLaunched`
    - `RocketMissionChanged`
    - `RocketSpeedIncreased`
    - `RocketSpeedDecreased`
    - `RocketExploded`

Benefits:
- Encapsulates business logic (order of messages, state transitions).
    No one touches `RocketStatus` or buffers directly.
- Invariants are protected (like no speed change after explosion).
- Keeps REST/controller/service layers thin.
- `ReentrantLock` ensuring only one writer per channel.
-  The root ensures message numbers are applied strictly in sequence.

---

This is not a classic Spring MVC app.  
I tried to make layered architecture with DDD: 
`controller (web layer) -> service (application layer) -> repository (persistence layer) -> storage (Redis or InMemory)`
- Controller only receives REST calls.
- Service orchestrates domain logic & persistence.
- Repository abstracts Redis/InMemory storage.
- Domain contains the real business logic (`RocketTracker`, events, snapshots).

---

## Concurrency & Data Structures

### ReentrantLock
- `RocketTracker` uses ReentrantLock to guarantee single writer per channel.
- The lock protects:
    - `status` (current rocket state),
    - `buffer` (pending out-of-order messages),
    - `appliedEvents` (history),
    - `lastApplied`.

This ensures no race conditions even with concurrent messages incoming.

---

### TreeMap

Used for buffering out-of-order messages (key = `messageNumber`).

I need the structure that can store entries sorted by the messageNumber (key). 

In hashmap keys are unsorted, harder to search for next.

In ArrayList if we receive messageNumber 1000, we will have to create list with 1001 elements (999 empty) - uneffective by memory and time.

Treemap  good choice. I started to use NavigableMap (instance by TreeMap) to guarantee sorted event buffering. Even though we currently only apply strictly sequential events (lastApplied+1), this design allows easy extension to handle missing or out-of-order events using methods like higherKey and lowerKey.

---

### Why this combination (ReentrantLock + TreeMap)?
- Multiple channels can be processed in parallel (no global lock).
- Within one channel, strict ordering is enforced.
- Ensures idempotency (duplicate messageNumbers don’t corrupt state).

---

##  Summary
- DDD aggregator (`RocketTracker`) ensures consistency.
- Event-sourcing + snapshotting.
- Concurrency handled by ReentrantLock + TreeMap.
- Two storage profiles: `inmemory` and `redis`.
- Frontend consumes REST APIs & visualizes rocket states.
- containerized + CI/CD pipeline.

## Testing

The project includes several tests:

### 1. Unit tests
- Located under `src/test/java/..`
- Validate domain logic (e.g. event ordering, state transitions in `RocketTracker`).
### 2. Integration tests
- Test the full Spring Boot context (controllers, services, repositories). 
- Redis-backed tests use @SpringBootTest with embedded Redis. 
- Ensure that REST APIs and persistence work correctly together.
### 3. Performance tests
- Located in a separate folder (performance_test).
- Stress-test concurrency and ordering guarantees.
- Simulate high message throughput and measures system stability 

#### Install k6:
On macOS (Homebrew):
```
brew install k6
```
On Linux:
```
sudo apt update
sudo apt install -y gnupg software-properties-common
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5A6A26E6A26B1E7
echo "deb https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt update
sudo apt install -y k6
```
On Windows (via Chocolatey - sorry was not able to test!):
```
choco install k6
```
Run
```
cd rocket
k6 run performance_test/load-test.js
```

### CI/CD
All tests run automatically on every push to main via GitHub Actions workflow:
-	build-and-test job: compiles + runs unit & integration tests.
-	docker job: builds and pushes Docker images only if tests pass.
This ensures that only verified builds are deployed to Docker Hub.

Run with:
  ```bash
  ./gradlew test
```
---

##  How to Run

### 1.  Run Backend & Dashboard with Docker Compose
```bash
docker compose up --build
```
 - Backend  http://localhost:8088
 - Dashboard  http://localhost:8081	
 - Redis  localhost:6379

### 2. Run with Docker Hub Images (no local build)
If you don’t want to build locally, pull prebuilt images from Docker Hub:
```bash
git clone https://github.com/DenisIgnatenko/rockets.git
cd rockets
docker compose -f docker-compose.hub.yml up
```
- Backend http://localhost:8088
- Dashboard http://localhost:8081
- Redis localhost:6379

### 3. Run backend locally with in-memory Profile (no dependencies)
Uses internal `ConcurrentHashMap`. Data is lost on restart.

```bash
cd rockets
./gradlew bootRun --args='--spring.profiles.active=inmemory'
```
- Backend: http://localhost:8088

### 4. Redis Profile (your own Redis)
```
export REDIS_HOST=your-redis-host
export REDIS_PORT=your-redis-port
cd rockets
./gradlew bootRun --args='--spring.profiles.active=redis'
```

### 4. Redis Profile (run local Redis)
Option A. macOS (brew)
```
brew install redis
brew services start redis
```
Option B. Docker (any OS)
```
docker run -d --name rockets_redis -p 6379:6379 redis:7.4
```

Then run backend:
```
cd rockets
./gradlew bootRun --args='--spring.profiles.active=redis'
```

### 5. How to Run Frontend
Local Dev (Vite)
```
cd rockets-dashboard
npm install
npm run dev
```

### 6. Run generator
```
cd rockets
./rockets launch "http://localhost:8088/messages" --message-delay=500ms --concurrency-level=1
```

### Thank you!