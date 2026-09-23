--liquibase formatted sql
--changeset JeDiFox:002_ddl_index_booking_id

CREATE INDEX index_booking_id
ON notifications (booking_id);