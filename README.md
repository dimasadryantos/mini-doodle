# 📅 Mini Doodle - Meeting Scheduling Service 🚀

A small meeting scheduling backend. Users manage FREE/BUSY time slots in their personal
calendar, book free slots as meetings, and query an aggregated availability view.

Built with Java 21, Spring Boot 3.4, PostgreSQL 16, Flyway, Testcontainers.

## 🏃 To run

First, you may need:

* JDK 21
* Maven
* Docker v2

Then just execute:

run the app

```
    - docker compose up --build
```

Once the server is up and running you can open:

```
http://localhost:8080/swagger-ui.html      (API docs, try requests here)
http://localhost:8080/actuator/prometheus  (metrics)
http://localhost:8080/actuator/health
```

run tests (Docker must be running, Testcontainers starts PostgreSQL by itself)

```
    - mvn test
```

## 🔌 How to use it

1. Register a user (a personal calendar is created behind the scenes)

```
POST /api/users
{"name":"Dimas","email":"dimas@example.com"}
```

2. Create available slots with a configurable duration

```
POST /api/users/{userId}/slots
{"startTime":"2026-03-02T09:00:00Z","durationMinutes":30}
```

3. Manage slots

```
GET    /api/users/{userId}/slots?from=...&to=...&status=FREE&page=0&size=50
PUT    /api/users/{userId}/slots/{slotId}            reschedule
PATCH  /api/users/{userId}/slots/{slotId}/status     {"status":"BUSY"} or "FREE"
DELETE /api/users/{userId}/slots/{slotId}
```

4. Book a free slot as a meeting

```
POST /api/users/{userId}/meetings
{"slotId":"...","title":"Design review","description":"...","participants":["a@x.com"]}

DELETE /api/users/{userId}/meetings/{meetingId}      cancel, slot becomes FREE again
```

5. Aggregated free/busy view

```
GET /api/users/{userId}/availability?from=2026-03-02T09:00:00Z&to=2026-03-02T11:00:00Z
```

Adjacent slots with the same status are merged into one interval, with total free and
busy minutes. Errors come back as RFC 7807 problem JSON (400 validation, 404 not found,
409 conflict).

## 🧑‍💻 About the challenge

I see the challenge mainly about data modeling and concurrency: hundreds of users with
thousands of slots means two requests hitting the same calendar at the same time is
normal, not an edge case. So I pushed the hard invariants down to the database where
races cannot bypass them:

* Slots in one calendar never overlap: PostgreSQL exclusion constraint on
  `(calendar_id, tstzrange(start, end))`.
* A slot is booked at most once: `UNIQUE (slot_id)` on meetings plus optimistic locking
  (`@Version`) on the slot.
* The hot query "slots of calendar X between A and B" is served by an index on
  `(calendar_id, start_time)`, bounded by a one-year frame and a 500-row page cap.

All violations surface as HTTP 409 so clients can retry or pick another slot.

## 🧑‍💻 Assumptions I had during coding

* Calendar is an internal concept only (as the task says), so every endpoint is addressed
  by `userId` and the calendar never appears in the API.
* BUSY means either "manually blocked" or "has a meeting". A slot with a meeting cannot be
  freed or deleted until the meeting is cancelled, so the two never disagree.
* Slot duration is between 5 minutes and 24 hours. Touching slots (09:00-09:30 and
  09:30-10:00) are fine, overlapping ones are rejected.
* No authentication: the `userId` in the path would normally come from the token.
* I wanted to avoid over-engineering, so there is no caching layer. The service is
  stateless and scales horizontally with PostgreSQL as the single source of truth.

## 🧑‍💻 Project architecture

In this project you will see:

👔 Domain

* JPA entities `User`, `Calendar`, `TimeSlot`, `Meeting` and the business rules that
  keep slot status and meeting consistent.

🤝 Api

* REST controllers, request/response DTOs and the global exception handler.

⚙️ Service

* Transactional use cases: users, slots, meetings, availability aggregation.

🗄️ Repository

* Spring Data repositories with the time-range queries.

📈 Metrics

* Micrometer counters for slots created, meetings booked/cancelled/rejected and a latency
  timer for availability queries, exposed at `/actuator/prometheus`.

## ✅ Testing strategy

* Integration tests run against a real PostgreSQL via Testcontainers so the constraints
  are actually exercised, not mocked.
* A concurrency test fires 20 parallel bookings at one slot and asserts exactly one wins.
* Unit tests for the pure interval-merge logic.

## 🔁 Feedback

I enjoyed this one, especially deciding where each invariant should live. Things I would
add with more time:

* Bulk slot generation ("every weekday 09:00-17:00 in 30-minute slots").
* Participant-side view: meetings a given email is invited to.
* Caching of availability for hot calendars, invalidated on slot writes.

I would really appreciate your feedback / comments so that I can also learn something
from this project. Thanks!
