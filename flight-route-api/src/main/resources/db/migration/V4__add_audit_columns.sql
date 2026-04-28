ALTER TABLE locations
    ADD COLUMN created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    ADD COLUMN last_modified_by VARCHAR(50) NOT NULL DEFAULT 'system';

ALTER TABLE transportations
    ADD COLUMN created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    ADD COLUMN last_modified_by VARCHAR(50) NOT NULL DEFAULT 'system';

ALTER TABLE users
    ADD COLUMN created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    ADD COLUMN last_modified_by VARCHAR(50) NOT NULL DEFAULT 'system';
