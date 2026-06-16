--liquibase formatted sql
--changeset JeDiFox:003_create_blacklisted_tokens_table

CREATE TABLE blacklisted_tokens (
    id UUID PRIMARY KEY,
    jti VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE UNIQUE INDEX idx_blacklisted_tokens_jti ON blacklisted_tokens (jti);