CREATE TABLE cart
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    created_at    datetime     NULL,
    last_modified datetime     NULL,
    is_deleted    BIT(1)       NOT NULL DEFAULT b'0',
    user_id       BIGINT       NULL,
    CONSTRAINT pk_cart PRIMARY KEY (id)
);

CREATE TABLE cart_item
(
    id            BIGINT   NOT NULL AUTO_INCREMENT,
    created_at    datetime NULL,
    last_modified datetime NULL,
    is_deleted    BIT(1)   NOT NULL DEFAULT b'0',
    quantity      INT      NULL,
    cart_id       BIGINT   NULL,
    product_id    BIGINT   NULL,
    CONSTRAINT pk_cart_item PRIMARY KEY (id)
);

ALTER TABLE cart
    ADD CONSTRAINT FK_CART_ON_USER FOREIGN KEY (user_id) REFERENCES user (id);

ALTER TABLE cart_item
    ADD CONSTRAINT FK_CART_ITEM_ON_CART FOREIGN KEY (cart_id) REFERENCES cart (id);

ALTER TABLE cart_item
    ADD CONSTRAINT FK_CART_ITEM_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product (id);

ALTER TABLE cart
    ADD CONSTRAINT uk_cart_user UNIQUE (user_id);

ALTER TABLE cart_item
    ADD CONSTRAINT uk_cart_item_cart_product UNIQUE (cart_id, product_id);

