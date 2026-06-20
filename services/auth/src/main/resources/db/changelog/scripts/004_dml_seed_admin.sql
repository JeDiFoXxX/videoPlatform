--liquibase formatted sql
--changeset JeDiFox:004_dml_seed_admin

INSERT INTO users (id, login, password_hash, role, created_at)
VALUES ('9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d',
        'admin',
        '$2a$10$gGGPYmy.nycfiWTg8OsiOemyLNgAFOrvcAZ1TcQZdcfD85r5P/E.C',
        'ADMIN',
        CURRENT_TIMESTAMP
        );