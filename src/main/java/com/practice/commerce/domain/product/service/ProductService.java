package com.practice.commerce.domain.product.service;

import com.practice.commerce.domain.product.exception.DuplicateProductException;
import com.practice.commerce.domain.category.exception.NotFoundCategoryException;
import com.practice.commerce.domain.user.exception.NotFoundUserException;
import com.practice.commerce.infrastructure.s3.S3UploadService;
import com.practice.commerce.domain.category.entity.Category;
import com.practice.commerce.domain.category.repository.CategoryRepository;
import com.practice.commerce.domain.product.controller.response.CreateProductResponse;
import com.practice.commerce.domain.product.controller.response.GetProductResponse;
import com.practice.commerce.domain.product.entity.Product;
import com.practice.commerce.domain.product.entity.ProductStatus;
import com.practice.commerce.domain.product.repository.ProductRepository;
import com.practice.commerce.domain.user.entity.User;
import com.practice.commerce.domain.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final S3UploadService s3UploadService;

    @Transactional
    public CreateProductResponse createProduct(
            String name,
            String description,
            UUID sellerId,
            UUID categoryId,
            ProductStatus status,
            List<MultipartFile> files
    ) {
        User seller = getSeller(sellerId);
        validateDuplicateProduct(name, seller);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundCategoryException("카테고리 조회에 실패했습니다. id = " + categoryId));
        Product product = Product.builder()
                .name(name)
                .description(description)
                .seller(seller)
                .status(status)
                .build();
        Product savedProduct = productRepository.save(product);

        // 이미지 업로드
        s3UploadService.upload(product, files);

        return new CreateProductResponse(savedProduct.getId());
    }

    @Transactional(readOnly = true)
    public GetProductResponse getProduct(UUID productId) {
        Product product = productRepository.findProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품 조회에 실패했습니다. id = " + productId));
        return GetProductResponse.of(product);
    }

    @Transactional(readOnly = true)
    public List<GetProductResponse> getProducts(UUID sellerId, Pageable pageable) {
        User seller = getSeller(sellerId);

        Page<Product> pageResult = productRepository.findProductsBySeller(seller, pageable);

        return pageResult.stream()
                .map(GetProductResponse::of)
                .toList();
    }

    private User getSeller(UUID sellerId) {
        return userRepository.findById(sellerId)
                .orElseThrow(() -> new NotFoundUserException("판매자 조회에 실패했습니다. id = " + sellerId));
    }

    private void validateDuplicateProduct(String name, User seller) throws DuplicateProductException {
        boolean existProduct = productRepository.existsBySellerAndName(seller, name);
        if (existProduct) {
            throw new DuplicateProductException("이미 등록되어 있는 상품입니다. name = " + name);
        }
    }
}
