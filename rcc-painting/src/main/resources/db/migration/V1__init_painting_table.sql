CREATE TABLE IF NOT EXISTS painting
(
    id          BINARY(16)   UNIQUE NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    content     LONGBLOB,
    artist_id   BINARY(16)   NOT NULL,
    museum_id   BINARY(16),
    PRIMARY KEY (id)
);

CREATE INDEX idx_painting_title ON painting(title);
CREATE INDEX idx_painting_artist_id ON painting(artist_id);
CREATE INDEX idx_painting_museum_id ON painting(museum_id);