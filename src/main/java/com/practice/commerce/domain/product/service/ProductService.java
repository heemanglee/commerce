package com.practice.commerce.domain.product.service;

import com.practice.commerce.common.exception.DuplicateProductException;
import com.practice.commerce.common.exception.NotFoundCategoryException;
import com.practice.commerce.common.exception.NotFoundUserException;
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

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public CreateProductResponse createProduct(
            String name,
            String description,
            UUID sellerId,
            UUID categoryId,
            ProductStatus status
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

        return new CreateProductResponse(savedProduct.getId());
    }

    @Transactional(readOnly = true)
    public GetProductResponse getProduct(UUID productId, UUID sellerId) {
        User seller = getSeller(sellerId);
        Product product = productRepository.findProductByIdAndSeller(productId, seller);

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
