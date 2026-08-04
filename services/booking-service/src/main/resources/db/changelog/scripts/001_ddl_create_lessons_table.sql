--liquibase formatted sql
--changeset JeDiFox:001_ddl_create_lessons_table

CREATE TABLE lessons (
    id          UUID PRIMARY KEY,
    student_id VARCHAR(255) NOT NULL,
    start_time TIMESTAMPTZ  NOT NULL,
    end_time   TIMESTAMPTZ  NOT NULL,
    status     VARCHAR(255) NOT NULL
);