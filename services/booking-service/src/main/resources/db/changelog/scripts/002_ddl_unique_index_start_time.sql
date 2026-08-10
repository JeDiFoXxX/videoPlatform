--liquibase formatted sql
--changeset JeDiFox:002_ddl_unique_index_start_time

CREATE UNIQUE INDEX unique_index_start_time
ON lessons (start_time)
WHERE status = 'SCHEDULED';