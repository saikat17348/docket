
CREATE TABLE app_users (
                           id BIGSERIAL PRIMARY KEY,
                           username VARCHAR(255) NOT NULL UNIQUE,
                           email VARCHAR(255) NOT NULL UNIQUE,
                           password VARCHAR(255) NOT NULL,
                           role VARCHAR(30) NOT NULL,
                           active BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at TIMESTAMP NOT NULL
);

CREATE TABLE cases (
                       id BIGSERIAL PRIMARY KEY,
                       customer_id VARCHAR(255) NOT NULL,
                       status VARCHAR(50) NOT NULL,
                       priority VARCHAR(10) NOT NULL,
                       assignee_id BIGINT REFERENCES app_users(id),
                       created_at TIMESTAMPTZ NOT NULL,
                       updated_at TIMESTAMPTZ NOT NULL,
                       due_at TIMESTAMPTZ NOT NULL,
                       sla_duration_seconds BIGINT NOT NULL,
                       version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE alerts (
                        id BIGSERIAL PRIMARY KEY,
                        alert_id VARCHAR(255) NOT NULL UNIQUE,
                        customer_id VARCHAR(255) NOT NULL,
                        rule_code VARCHAR(255) NOT NULL,
                        score INTEGER NOT NULL CHECK (score BETWEEN 0 AND 100),
                        amount NUMERIC(19,2) NOT NULL,
                        currency VARCHAR(3) NOT NULL,
                        raised_at TIMESTAMPTZ NOT NULL,
                        case_id BIGINT NOT NULL REFERENCES cases(id)
);

CREATE TABLE case_events (
                             id BIGSERIAL PRIMARY KEY,
                             case_id BIGINT NOT NULL REFERENCES cases(id),
                             actor VARCHAR(255) NOT NULL,
                             action VARCHAR(100) NOT NULL,
                             from_status VARCHAR(50),
                             to_status VARCHAR(50) NOT NULL,
                             note VARCHAR(1000),
                             at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_cases_customer_status
    ON cases(customer_id, status);

CREATE INDEX idx_cases_queue
    ON cases(status, priority, due_at ASC);

CREATE INDEX idx_alerts_case_id
    ON alerts(case_id);

CREATE INDEX idx_case_events_case_at
    ON case_events(case_id, at ASC);
