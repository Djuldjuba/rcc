CREATE TABLE IF NOT EXISTS `user`
(
    id        BINARY(16)   UNIQUE NOT NULL,
    username  VARCHAR(50)  UNIQUE NOT NULL,
    firstname VARCHAR(255),
    lastname  VARCHAR(255),
    avatar    LONGBLOB,
    PRIMARY KEY (id)
);

CREATE INDEX idx_user_username ON `user`(username);