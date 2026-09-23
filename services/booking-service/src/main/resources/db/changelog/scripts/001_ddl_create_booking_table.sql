--liquibase formatted sql
--changeset JeDiFox:001_ddl_create_booking_table

CREATE TABLE booking (
    id          UUID PRIMARY KEY,
    student_id  UUID NOT NULL,
    start_time  TIMESTAMP NOT NULL,
    end_time    TIMESTAMP NOT NULL,
    status      VARCHAR(255) NOT NULL
);