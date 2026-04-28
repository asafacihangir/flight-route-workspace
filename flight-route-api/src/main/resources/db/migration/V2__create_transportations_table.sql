CREATE TABLE transportations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    origin_id BIGINT NOT NULL,
    destination_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    operating_days VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_transportations_origin FOREIGN KEY (origin_id) REFERENCES locations(id),
    CONSTRAINT fk_transportations_destination FOREIGN KEY (destination_id) REFERENCES locations(id)
) ENGINE=InnoDB;

CREATE INDEX idx_transportations_origin ON transportations (origin_id);
CREATE INDEX idx_transportations_destination ON transportations (destination_id);
CREATE INDEX idx_transportations_type ON transportations (type);
