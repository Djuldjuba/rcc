CREATE TABLE IF NOT EXISTS `log`
(
    id         BINARY(16)  NOT NULL,
    user_id    VARCHAR(36),
    photo_id   VARCHAR(36),
    event_type VARCHAR(50) NOT NULL,
    event_date DATETIME    NOT NULL,
    log_date   DATETIME    NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_log_event_type ON `log`(event_type);
CREATE INDEX idx_log_user_id ON `log`(user_id);
CREATE INDEX idx_log_event_date ON `log`(event_date);