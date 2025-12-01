CREATE TABLE payment
(
    id             BIGINT       NOT NULL,
    name           VARCHAR(255) NULL,
    created_at     datetime     NULL,
    last_modified  datetime     NULL,
    is_deleted     BIT(1)       NOT NULL,
    amount         DOUBLE       NULL,
    method         VARCHAR(50)  NULL,
    status         VARCHAR(50)  NULL,
    transaction_id VARCHAR(255) NULL,
    payment_date   datetime     NULL,
    order_id       BIGINT       NULL,
    CONSTRAINT pk_payment PRIMARY KEY (id)
);

ALTER TABLE payment
    ADD CONSTRAINT FK_PAYMENT_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id);


