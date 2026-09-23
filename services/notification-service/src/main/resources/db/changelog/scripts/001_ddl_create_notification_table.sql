--liquibase formatted sql
--changeset JeDiFox:001_ddl_create_notification_table

CREATE TABLE notification (
    id          UUID PRIMARY KEY,
    chat_id     BIGINT NOT NULL,
    booking_id  UUID NOT NULL,
    first_name  VARCHAR(255) NOT NULL,
    start_time  TIMESTAMP NOT NULL,
    end_time    TIMESTAMP NOT NULL
);