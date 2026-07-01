--liquibase formatted sql
--changeset JeDiFox:004_dml_seed_admin

INSERT INTO users (id, login, password_hash, role, created_at)
VALUES ('9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d',
        'admin',
        '${admin.password.hash}',
        'ADMIN',
        CURRENT_TIMESTAMP
        );