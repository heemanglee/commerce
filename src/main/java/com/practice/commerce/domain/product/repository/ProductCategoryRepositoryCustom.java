package com.practice.commerce.domain.product.repository;

import com.practice.commerce.domain.product.entity.Product;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductCategoryRepositoryCustom {

    Page<Product> searchProducts(UUID categoryId, String searchKeyword, Pageable pageable);
}