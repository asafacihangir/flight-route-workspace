CREATE TABLE locations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    country VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_locations_code UNIQUE (code)
) ENGINE=InnoDB;

CREATE INDEX idx_locations_city ON locations (city);
CREATE INDEX idx_locations_country ON locations (country);
