package com.practice.commerce.domain.product.service;

import com.practice.commerce.domain.category.entity.Category;
import com.practice.commerce.domain.category.exception.NotFoundCategoryException;
import com.practice.commerce.domain.category.repository.CategoryRepository;
import com.practice.commerce.domain.product.controller.request.ReorderMediaRequest.MediaPosition;
import com.practice.commerce.domain.product.controller.response.CreateProductResponse;
import com.practice.commerce.domain.product.controller.response.GetProductResponse;
import com.practice.commerce.domain.product.entity.Product;
import com.practice.commerce.domain.product.entity.ProductMedia;
import com.practice.commerce.domain.product.entity.ProductStatus;
import com.practice.commerce.domain.product.exception.DuplicateProductException;
import com.practice.commerce.domain.product.exception.InvalidProductMediaException;
import com.practice.commerce.domain.product.repository.ProductMediaRepository;
import com.practice.commerce.domain.product.repository.ProductRepository;
import com.practice.commerce.domain.user.entity.User;
import com.practice.commerce.domain.user.exception.NotFoundUserException;
import com.practice.commerce.domain.user.repository.UserRepository;
import com.practice.commerce.infrastructure.s3.S3UploadService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final ProductMediaRepository productMediaRepository;

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

    @Transactional
    public void updateMediaPositions(UUID productId, List<MediaPosition> mediaPositions) {
        List<ProductMedia> productMedias = getAndValidateProductMedias(productId, mediaPositions);

        // 각 미디어의 position 업데이트
        // (product_id, positon)은 uk로 설정되어 있으므로 이미지의 position을 임시 값으로 변경함.
        int tempPosition = 100;
        Map<UUID, ProductMedia> mediaMap = new HashMap<>();
        for (ProductMedia productMedia : productMedias) {
            mediaMap.put(productMedia.getId(), productMedia);
            productMedia.updatePosition(tempPosition++);
        }
        productMediaRepository.flush();

        for (MediaPosition mediaPosition : mediaPositions) {
            ProductMedia productMedia = mediaMap.get(mediaPosition.id());
            productMedia.updatePosition(mediaPosition.position());
        }
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

    private List<ProductMedia> getAndValidateProductMedias(UUID productId, List<MediaPosition> mediaPositions) {
        List<UUID> mediaIds = mediaPositions.stream()
                .map(MediaPosition::id)
                .toList();

        // 요청된 이미지가 상품에 포함되는지 검증
        List<ProductMedia> productMedias = productMediaRepository.findByProductId(productId);
        validateProductMedia(mediaPositions, mediaIds, productMedias);

        return productMedias;
    }

    private void validateProductMedia(List<MediaPosition> mediaPositions,
                                      List<UUID> mediaIds,
                                      List<ProductMedia> productMedias
    ) {
        if (mediaIds.size() != productMedias.size()) {
            throw new InvalidProductMediaException(
                    "MEDIA_NOT_BELONG_TO_PRODUCT",
                    "요청된 이미지 중 일부가 해당 상품에 포함되어 있지 않습니다."
            );
        }

        // position 검증: 0부터 연속적이고 중복이 없는지
        List<Integer> positions = mediaPositions.stream()
                .map(MediaPosition::position)
                .sorted()
                .toList();

        for (int i = 0; i < positions.size(); i++) {
            if (positions.get(i) != i) {
                throw new InvalidProductMediaException(
                        "INVALID_MEDIA_POSITION",
                        String.format("position은 0부터 연속적이어야 합니다. 예상: %d, 실제: %d", i, positions.get(i))
                );
            }
        }
    }
}
