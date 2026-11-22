# Mini Doodle – Meeting Scheduling Service

A small, high-throughput meeting scheduling backend built with **Java 21** and **Spring Boot 3**.

Users own a personal calendar in which they manage time slots. A free slot can be
booked as a meeting (title, description, participants). The service exposes an
aggregated free/busy view for any time frame.

## Goals

- Time slot management: create slots with a configurable duration, update, delete,
  and mark them free or busy.
- Meeting scheduling: convert an available slot into a meeting with details.
- Availability query: aggregated free/busy view over a selected time frame.
- Persistence in PostgreSQL, runnable locally with `docker compose up`.
- Metrics (Prometheus), OpenAPI documentation, and automated tests.

## Status

Work in progress – see the commit history for the incremental steps.
