CREATE TABLE password_reset_token
(
    id            BIGINT       NOT NULL,
    name          VARCHAR(255) NULL,
    created_at    datetime     NULL,
    last_modified datetime     NULL,
    is_deleted    BIT(1)       NOT NULL,
    token         VARCHAR(255) NOT NULL,
    expiry_date   datetime     NULL,
    used          BIT(1)       NOT NULL,
    user_id       BIGINT       NOT NULL,
    CONSTRAINT pk_password_reset_token PRIMARY KEY (id)
);

ALTER TABLE password_reset_token
    ADD CONSTRAINT uk_password_reset_token_token UNIQUE (token);

ALTER TABLE password_reset_token
    ADD CONSTRAINT FK_PASSWORD_RESET_TOKEN_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);


