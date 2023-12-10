-- liquibase formatted sql

-- changeset mpankov:1
CREATE TABLE notification_task
(
    id       SERIAL PRIMARY KEY,
    chat_id   BIGINT                      NOT NULL,
    text     TEXT                        NOT NULL,
    date_time TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX date_time_index ON notification_task(date_time)
-- changeset mpankov:2
ALTER TABLE notification_task ADD COLUMN has_been_sent BOOLEAN DEFAULT false
