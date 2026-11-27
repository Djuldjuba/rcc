CREATE TABLE IF NOT EXISTS museum
(
    id          BINARY(16)   UNIQUE NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    city        VARCHAR(255),
    photo       LONGBLOB,
    country_id  BINARY(16)   NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_museum_title ON museum(title);
CREATE INDEX idx_museum_country_id ON museum(country_id);