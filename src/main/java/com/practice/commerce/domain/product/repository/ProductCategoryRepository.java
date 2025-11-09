package com.practice.commerce.domain.product.repository;

import com.practice.commerce.domain.product.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long>,
        ProductCategoryRepositoryCustom {

}
