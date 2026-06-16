--liquibase formatted sql
--changeset JeDiFox:001_create_users_table

create TABLE users (
        id UUID PRIMARY KEY,
        login VARCHAR(20) NOT NULL UNIQUE,
        password_hash VARCHAR(255) NOT NULL,
        role VARCHAR(20) NOT NULL,
        created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);