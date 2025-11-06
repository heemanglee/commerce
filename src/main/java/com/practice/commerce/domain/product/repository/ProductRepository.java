package com.practice.commerce.domain.product.repository;

import com.practice.commerce.domain.product.entity.Product;
import com.practice.commerce.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsBySellerAndName(User seller, String name);

    Optional<Product> findProductByIdAndSeller(UUID productId, User seller);

    Page<Product> findProductsBySeller(User seller, Pageable pageable);

    Optional<Product> findProductById(UUID productId);

    List<Product> seller(User seller);

    Optional<Product> findProductByIdAndSellerId(UUID productId, UUID sellerId);
}
