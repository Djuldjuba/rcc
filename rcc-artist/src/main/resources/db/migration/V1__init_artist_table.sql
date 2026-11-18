CREATE TABLE IF NOT EXISTS artist
(
    id        BINARY(16)   UNIQUE NOT NULL,
    name      VARCHAR(255) UNIQUE NOT NULL,
    biography VARCHAR(2000) NOT NULL,
    photo     LONGBLOB,
    PRIMARY KEY (id)
);

CREATE INDEX idx_artist_name ON artist(name);