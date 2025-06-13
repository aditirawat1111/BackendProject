package com.aditi.backendcapstoneproject.repository;

public class CustomQuery {
    public static final String GET_PRODUCT_FROM_CATEGORY_NAME=
            "select * product from product where category_id in (select category_id from category where name=:categoryName))";
}
