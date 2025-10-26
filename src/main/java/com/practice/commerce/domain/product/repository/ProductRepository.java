package com.practice.commerce.domain.product.repository;

import com.practice.commerce.domain.product.entity.Product;
import com.practice.commerce.domain.user.entity.User;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsBySellerAndName(User seller, String name);

    Product findProductByIdAndSeller(UUID productId, User seller);

    Page<Product> findProductsBySeller(User seller, PageRequest pageable);
}
