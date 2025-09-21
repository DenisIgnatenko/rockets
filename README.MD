# Lunar Rockets API & Dashboard

This project simulates rocket telemetry ingestion, processing, and visualization.  
It consists of two parts:

- **Backend (Spring Boot + Redis/InMemory)** — processes telemetry events and provides REST APIs.
- **Frontend (React/Vite Dashboard)** — displays rocket states and history in real time.

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

- Encapsulates business logic (order of messages, state transitions).
    No one touches `RocketStatus` or buffers directly.
- Invariants are protected (like no speed change after explosion).
- Keeps REST/controller/service layers thin.
- `ReentrantLock` ensuring only one writer per channel.
-  The root ensures message numbers are applied strictly in sequence.
---

## Architecture
![Diagram](src/main/resources/static/Diagram.png)

This is not a classic Spring MVC app.  
I tried to make layered architecture with DDD: 
` controller (web layer) -> service (application layer) -> repository (persistence layer) -> storage (Redis or InMemory)`
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
- Used for buffering out-of-order messages (key = `messageNumber`).
I need the structure that can store entries sorted by the messageNumber (key). 
In hashmap keys are unsorted, harder to search for next 
In ArrayList if we receive messageNumber 1000, we will have to create list with 1001
elements (999 empty) - uneffective by memory and time.
Treemap - good choice.
I started to use NavigableMap (backed by TreeMap) to guarantee sorted event buffering.
Even though we currently only apply strictly sequential events (lastApplied+1), this
design allows easy extension to handle missing or out-of-order events using methods 
like higherKey and lowerKey.
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
- 
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
docker compose -f docker-compose.hub.yml up
```
Backend http://localhost:8088
Dashboard http://localhost:8081
Redis localhost:6379

### 3. Run backend locally with in-memory Profile (no dependencies)
Uses internal `ConcurrentHashMap`. Data is lost on restart.

```bash
cd rockets
./gradlew bootRun --args='--spring.profiles.active=inmemory'
```
Backend → http://localhost:8088

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
cd rockets-frontend
npm install
npm run dev
```