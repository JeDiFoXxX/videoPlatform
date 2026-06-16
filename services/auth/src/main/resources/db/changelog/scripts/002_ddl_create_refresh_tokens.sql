--liquibase formatted sql
--changeset JeDiFox:002_create_refresh_tokens_table

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    user_id UUID NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX idx_refresh_tokens_token ON refresh_tokens (token);