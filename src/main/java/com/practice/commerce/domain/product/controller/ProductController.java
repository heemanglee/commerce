package com.practice.commerce.domain.product.controller;

import com.practice.commerce.domain.product.controller.request.CreateProductRequest;
import com.practice.commerce.domain.product.controller.request.ReorderMediaRequest;
import com.practice.commerce.domain.product.controller.response.CreateProductResponse;
import com.practice.commerce.domain.product.controller.response.GetProductResponse;
import com.practice.commerce.domain.product.service.ProductService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
    @PatchMapping("/{productId}/media/reorder")
    public ResponseEntity<Void> updateMediaOrder(
            @PathVariable UUID productId,
            @Valid @RequestBody ReorderMediaRequest request
    ) {
        productService.updateMediaPositions(productId, request.mediaPositions());
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

    @GetMapping
    public ResponseEntity<List<GetProductResponse>> getProducts(
            @AuthenticationPrincipal UUID sellerId,
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.ASC
            ) Pageable pageable
    ) {
        List<GetProductResponse> response = productService.getProducts(sellerId, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
