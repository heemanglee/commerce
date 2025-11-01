package com.practice.commerce.domain.category.service;

import com.practice.commerce.domain.category.exception.DuplicateCategoryNameException;
import com.practice.commerce.domain.category.exception.NotFoundCategoryException;
import com.practice.commerce.domain.category.controller.request.CategoryStatusRequest;
import com.practice.commerce.domain.category.controller.response.CategoryStatusResponse;
import com.practice.commerce.domain.category.controller.response.CreateCategoryResponse;
import com.practice.commerce.domain.category.entity.Category;
import com.practice.commerce.domain.category.repository.CategoryRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CreateCategoryResponse create(
            String name,
            CategoryStatusRequest status,
            UUID parentId
    ) {
        Category parent = validateAndGetParent(parentId);
        validateDuplicateCategoryName(name, parent);

        Category category = Category.builder()
                .name(name)
                .status(status.toEntityStatus())
                .parent(parent)
                .build();
        Category savedCategory = categoryRepository.save(category);

        return toResponse(savedCategory);
    }

    private CreateCategoryResponse toResponse(Category category) {
        CategoryStatusResponse status = CategoryStatusResponse.valueOf(category.getStatus().name());
        UUID parentId = category.getParent() == null ? null : category.getParent().getId();
        return new CreateCategoryResponse(category.getId(), category.getName(), status, parentId);
    }

    private void validateDuplicateCategoryName(String name, Category parent) {
        boolean isExistCategory = categoryRepository.existsByNameAndParentId(name, parent);
        if (isExistCategory) {
            throw new DuplicateCategoryNameException("중복 카테고리를 생성할 수 없습니다. category = " + name);
        }
    }

    private Category validateAndGetParent(UUID parentId) {
        if (parentId == null) {
            return null;
        }

        return categoryRepository.findByParentId_Id(parentId)
                .orElseThrow(() -> new NotFoundCategoryException("부모 카테고리 조회 실패, parentId=" + parentId));
    }
}
