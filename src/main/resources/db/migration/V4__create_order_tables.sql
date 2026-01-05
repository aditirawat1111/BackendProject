CREATE TABLE orders
(
    id              BIGINT       NOT NULL,
    name            VARCHAR(255) NULL,
    created_at      datetime     NULL,
    last_modified   datetime     NULL,
    is_deleted      BIT(1)       NOT NULL,
    order_date      datetime     NULL,
    status          VARCHAR(50)  NULL,
    total_amount    DOUBLE       NULL,
    delivery_address VARCHAR(500) NULL,
    user_id         BIGINT       NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id)
);

CREATE TABLE order_item
(
    id            BIGINT       NOT NULL,
    name          VARCHAR(255) NULL,
    created_at    datetime     NULL,
    last_modified datetime     NULL,
    is_deleted    BIT(1)       NOT NULL,
    quantity      INT          NULL,
    price         DOUBLE       NULL,
    order_id      BIGINT       NULL,
    product_id    BIGINT       NULL,
    CONSTRAINT pk_order_item PRIMARY KEY (id)
);

ALTER TABLE orders
    ADD CONSTRAINT FK_ORDERS_ON_USER FOREIGN KEY (user_id) REFERENCES `users` (id);

ALTER TABLE order_item
    ADD CONSTRAINT FK_ORDER_ITEM_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id);

ALTER TABLE order_item
    ADD CONSTRAINT FK_ORDER_ITEM_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product (id);


