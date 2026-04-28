INSERT INTO users (username, password_hash, role, created_at, updated_at, version, created_by, last_modified_by)
VALUES (
    'systemagency',
    '$2a$10$Goe4TBaZMp52bMDnp/2FnOqAJr4w0JNqCGGDBFlmauRgZSFWzb2pi',
    'AGENCY',
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6),
    0,
    'system',
    'system'
);
