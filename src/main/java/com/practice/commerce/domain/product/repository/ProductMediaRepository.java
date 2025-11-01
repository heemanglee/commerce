package com.practice.commerce.domain.product.repository;

import com.practice.commerce.domain.product.entity.ProductMedia;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductMediaRepository extends JpaRepository<ProductMedia, UUID> {
}
