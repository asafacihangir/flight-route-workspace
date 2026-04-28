INSERT INTO users (username, password_hash, role, created_at, updated_at, version, created_by, last_modified_by)
VALUES (
    'systemadmin',
    '$2a$10$ECop8dWpyXicEgCWDMQPSellMP7XAIbOub1Mb0Ev1gcmcXfD7.74S',
    'ADMIN',
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6),
    0,
    'system',
    'system'
);
