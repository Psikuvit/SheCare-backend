-- V2__seed_data.sql
-- Seed admin user
INSERT INTO users (id, email, password_hash, name, enabled, created_at, updated_at)
VALUES (
    '550e8400-e29b-41d4-a716-446655440001',
    'admin@shecare.com',
    '$2a$10$S8lQqkfq2O0pCLvv7QXU/e.5L3L3J9LX7n5m9K8pQ5L8R2e3S3S3S',
    'Admin User',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO user_roles (user_id, role)
VALUES ('550e8400-e29b-41d4-a716-446655440001', 'ROLE_ADMIN');

-- Seed doctor user
INSERT INTO users (id, email, password_hash, name, enabled, created_at, updated_at)
VALUES (
    '550e8400-e29b-41d4-a716-446655440002',
    'doctor@shecare.com',
    '$2a$10$S8lQqkfq2O0pCLvv7QXU/e.5L3L3J9LX7n5m9K8pQ5L8R2e3S3S3S',
    'Dr. Jane Smith',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO user_roles (user_id, role)
VALUES ('550e8400-e29b-41d4-a716-446655440002', 'ROLE_DOCTOR');

-- Seed patient user
INSERT INTO users (id, email, password_hash, name, enabled, created_at, updated_at)
VALUES (
    '550e8400-e29b-41d4-a716-446655440003',
    'patient@shecare.com',
    '$2a$10$S8lQqkfq2O0pCLvv7QXU/e.5L3L3J9LX7n5m9K8pQ5L8R2e3S3S3S',
    'Jane Patient',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO user_roles (user_id, role)
VALUES ('550e8400-e29b-41d4-a716-446655440003', 'ROLE_PATIENT');

-- All passwords are: Password123!
-- This is a hash of "Password123!" using BCrypt

