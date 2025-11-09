package com.practice.commerce.domain.product.controller;

import com.practice.commerce.domain.product.controller.request.CreateProductRequest;
import com.practice.commerce.domain.product.controller.request.DeleteProductMediaRequest;
import com.practice.commerce.domain.product.controller.request.ReorderMediaRequest;
import com.practice.commerce.domain.product.controller.response.CreateProductResponse;
import com.practice.commerce.domain.product.controller.response.GetProductResponse;
import com.practice.commerce.domain.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Product API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateProductResponse> createProduct(
        @Valid @RequestPart("data") CreateProductRequest request,
        @RequestPart(value = "files", required = false) List<MultipartFile> files,
        @AuthenticationPrincipal UUID sellerId
    ) {
        CreateProductResponse response = productService.createProduct(
            request.name(),
            request.description(),
            sellerId,
            request.categoryId(),
            request.status(),
            files
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('SELLER')")
    @PatchMapping("/{productId}/media")
    public ResponseEntity<Void> updateProductMedia(
        @PathVariable UUID productId,
        @RequestPart(value = "data", required = false) ReorderMediaRequest request,
        @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        productService.updateProductMedia(productId, request == null ? null : request.mediaPositions(), files);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("hasRole('SELLER')")
    @DeleteMapping("/{productId}/media")
    public ResponseEntity<Void> deleteProductMedia(
        @PathVariable UUID productId,
        @Valid @RequestBody DeleteProductMediaRequest request
    ) {
        productService.deleteProductMedia(productId, request.medias());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("hasRole('SELLER')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
        @PathVariable UUID productId,
        @AuthenticationPrincipal UUID sellerId
    ) {
        productService.deleteProduct(productId, sellerId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/{product_id}")
    public ResponseEntity<GetProductResponse> getProduct(
        @PathVariable("product_id") UUID productId
    ) {
        GetProductResponse response = productService.getProduct(productId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
        summary = "상품 목록 조회",
        description = "카테고리와 검색어 조건에 따라 상품을 조회합니다.\n" +
            "검색어(search)를 입력하지 않으면 해당 카테고리의 상품을 반환합니다.",
        parameters = {
            @Parameter(
                name = "categoryId",
                description = "카테고리 ID",
                example = "b7f9c6c3-7148-4c6a-a7a7-49cdb9d97555",
                in = ParameterIn.QUERY
            ),
            @Parameter(
                name = "search",
                description = "상품 이름",
                example = "프린팅 티셔츠",
                in = ParameterIn.QUERY
            )
        }
    )
    @GetMapping
    public ResponseEntity<List<GetProductResponse>> getProducts(
        @RequestParam(required = false) UUID categoryId,
        @RequestParam(required = false) String search,
        @PageableDefault(
            page = 0,
            size = 10,
            sort = "createdAt",
            direction = Sort.Direction.ASC
        ) Pageable pageable
    ) {
        List<GetProductResponse> response = productService.searchProducts(categoryId, search, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
