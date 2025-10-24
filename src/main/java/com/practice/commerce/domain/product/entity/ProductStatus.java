package com.practice.commerce.domain.product.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ProductStatus {

    ON_SALE("판매중"),
    OUT_OF_STOCK("품절"),
    STOPPED("판매 중단");

    private final String status;

}
