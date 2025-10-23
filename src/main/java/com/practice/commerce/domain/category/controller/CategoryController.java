package com.practice.commerce.domain.category.controller;

import com.practice.commerce.domain.category.controller.request.CreateCategoryRequest;
import com.practice.commerce.domain.category.controller.response.CreateCategoryResponse;
import com.practice.commerce.domain.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CreateCategoryResponse> create(
            @Valid @RequestBody CreateCategoryRequest request) {
        CreateCategoryResponse response = categoryService.create(
                request.name(),
                request.status(),
                request.parentId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
