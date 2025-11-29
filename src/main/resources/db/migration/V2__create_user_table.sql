CREATE TABLE `user`
(
    id            BIGINT       NOT NULL,
    name          VARCHAR(255) NULL,
    created_at    datetime     NULL,
    last_modified datetime     NULL,
    is_deleted    BIT(1)       NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password      VARCHAR(255) NOT NULL,
    phone_number  VARCHAR(255) NULL,
    address       VARCHAR(255) NULL,
    role          VARCHAR(50)  NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

ALTER TABLE `user`
    ADD CONSTRAINT uk_user_email UNIQUE (email);

