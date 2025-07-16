CREATE TABLE user_token (
    user_token_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_t_id BIGINT NOT NULL,
    token VARCHAR(512) NOT NULL,
    expired_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_t_id) REFERENCES user_t(user_t_id)
);