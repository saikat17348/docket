
# Docket – Alert-to-Case Management System

A Spring Boot-based financial crime and fraud investigation system that ingests alerts, groups them into investigation cases, manages case lifecycles through role-based workflows, and maintains an audit trail.

## Tech Stack

- Java 21
- Spring Boot 3
- Spring Data JPA
- Spring Security
- PostgreSQL
- Flyway
- Maven

## Features

- Alert ingestion with alert ID idempotency.
- Customer-based case grouping.
- Priority calculation based on alert score.
- SLA-based due date calculation.
- Role-aware case lifecycle management.
- Append-only case event audit timeline.
- Optimistic locking for concurrent updates.
- Filtered and paginated case queue.

## Case Priority

| Score | Priority | Default SLA |
|---|---|---|
| 80–100 | P1 | 4 hours |
| 50–79 | P2 | 24 hours |
| 0–49 | P3 | 72 hours |

## Case State Transitions

| Action | Allowed From | Result |
|---|---|---|
| ASSIGN | NEW, ASSIGNED, ESCALATED | ASSIGNED |
| START_REVIEW | ASSIGNED | IN_REVIEW |
| REQUEST_INFO | IN_REVIEW | PENDING_INFO |
| RESUME | PENDING_INFO | IN_REVIEW |
| ESCALATE | IN_REVIEW | ESCALATED |
| CLOSE_FALSE_POSITIVE | IN_REVIEW, ESCALATED | CLOSED_FALSE_POSITIVE |
| CLOSE_FRAUD | IN_REVIEW, ESCALATED | CLOSED_CONFIRMED_FRAUD |

Closed statuses are terminal.

## Seeded Users

| Username | Role | Password |
|---|---|---|
| analyst1 | ANALYST | Password@123 |
| analyst2 | ANALYST | Password@123 |
| supervisor1 | SUPERVISOR | Password@123 |

## Local Setup

1. Install Java 21 and PostgreSQL.
2. Create a PostgreSQL database named `docket`.
3. Configure database credentials in `application.properties`.
4. Run the application:

```bash
mvn spring-boot:run
```

Flyway executes database migrations automatically.

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/alerts | Ingest alert |
| GET | /api/cases | Filter case queue |
| GET | /api/cases/{id} | Get case details |
| POST | /api/cases/{id}/actions | Perform case action |

## Queue Index

The composite index `idx_cases_queue` uses status, priority, and due_at.

The leading columns support status and priority filtering, while due_at supports queue ordering. Actual query plans should be checked with PostgreSQL EXPLAIN ANALYZE.

## Testing

Run:

```bash
mvn test
```

## AI Usage

See [AI_USAGE.md](AI_USAGE.md) for AI assistance and corrections.
