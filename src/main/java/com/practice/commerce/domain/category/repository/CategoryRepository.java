package com.practice.commerce.domain.category.repository;

import com.practice.commerce.domain.category.entity.Category;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findByParentId(UUID parentId);

    boolean existsByName(String name);
}
