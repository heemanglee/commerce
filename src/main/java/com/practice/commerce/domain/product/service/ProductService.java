package com.practice.commerce.domain.product.service;

import static com.practice.commerce.domain.product.entity.ProductStatus.STOPPED;

import com.practice.commerce.domain.category.entity.Category;
import com.practice.commerce.domain.category.exception.NotFoundCategoryException;
import com.practice.commerce.domain.category.repository.CategoryRepository;
import com.practice.commerce.domain.product.controller.request.DeleteProductMediaRequest.DeleteMedia;
import com.practice.commerce.domain.product.controller.request.ReorderMediaRequest.MediaPosition;
import com.practice.commerce.domain.product.controller.response.CreateProductResponse;
import com.practice.commerce.domain.product.controller.response.GetProductResponse;
import com.practice.commerce.domain.product.entity.Product;
import com.practice.commerce.domain.product.entity.ProductCategory;
import com.practice.commerce.domain.product.entity.ProductMedia;
import com.practice.commerce.domain.product.entity.ProductStatus;
import com.practice.commerce.domain.product.event.ProductMediaDeletedEvent;
import com.practice.commerce.domain.product.exception.DuplicateProductException;
import com.practice.commerce.domain.product.exception.InvalidProductMediaException;
import com.practice.commerce.domain.product.exception.NotFoundProductException;
import com.practice.commerce.domain.product.repository.ProductCategoryRepository;
import com.practice.commerce.domain.product.repository.ProductMediaRepository;
import com.practice.commerce.domain.product.repository.ProductRepository;
import com.practice.commerce.domain.user.entity.User;
import com.practice.commerce.domain.user.exception.NotFoundUserException;
import com.practice.commerce.domain.user.repository.UserRepository;
import com.practice.commerce.infrastructure.message.MessageQueueService;
import com.practice.commerce.infrastructure.message.S3DeletionMessage;
import com.practice.commerce.infrastructure.s3.S3UploadService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final S3UploadService s3UploadService;
    private final ProductMediaRepository productMediaRepository;
    private final MessageQueueService messageQueueService;
    private final ApplicationEventPublisher applicationEventPublisher;

    private static final int TEMP_POSITION_START = 1000;
    private final ProductMediaService productMediaService;

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

        ProductCategory productCategory = ProductCategory.builder()
                .product(product)
                .category(category)
                .build();
        productCategoryRepository.save(productCategory);

        // 이미지 업로드
        s3UploadService.upload(product, files, 0);

        return new CreateProductResponse(savedProduct.getId());
    }

    @Transactional(readOnly = true)
    public GetProductResponse getProduct(UUID productId) {
        Product product = getProductById(productId);
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
    public void updateProductMedia(UUID productId, List<MediaPosition> mediaPositions, List<MultipartFile> files) {
        Product product = getProductById(productId);

        boolean hasFiles = hasFiles(files);
        boolean hasMediaPositions = hasMediaPositions(mediaPositions);

        if (hasFiles && hasMediaPositions) {
            addNewMediaAndUpdatePosition(mediaPositions, files, product);
        } else if (hasMediaPositions) {
            updatePosition(mediaPositions, product);
        } else {
            addNewMediaOnly(product, files);
        }

        throw new InvalidProductMediaException(
                "INVALID_REQUEST",
                "새로운 이미지 또는 순서 정보 중 하나는 제공되어야 합니다."
        );
    }

    @Transactional
    public void deleteProductMedia(UUID productId, List<DeleteMedia> medias) {
        List<UUID> mediaIdsToDelete = medias.stream()
                .map(DeleteMedia::id)
                .toList();

        // 삭제 요청한 미디어가 모두 상품에 포함되는지 확인
        List<ProductMedia> productMedias = productMediaRepository.findByProductId(productId);
        List<ProductMedia> mediaToDelete = productMedias.stream()
                .filter(media -> mediaIdsToDelete.contains(media.getId()))
                .toList();

        if (mediaToDelete.size() != mediaIdsToDelete.size()) {
            throw new InvalidProductMediaException(
                    "MEDIA_NOT_BELONG_TO_PRODUCT",
                    "요청된 이미지 중 일부가 해당 상품에 포함되어 있지 않습니다."
            );
        }

        // 미디어 삭제 처리
        mediaToDelete.forEach(ProductMedia::markAsDeleted);

        // SQS에 S3 삭제 메시지 발행
        List<S3DeletionMessage> deletionMessages = mediaToDelete.stream()
                .map(media -> S3DeletionMessage.builder()
                        .mediaId(media.getId())
                        .bucketName(media.getBucketName())
                        .bucketKey(media.getBucketObjectKey())
                        .retryCount(0)
                        .build()
                )
                .toList();

        messageQueueService.sendS3DeletionMessages(deletionMessages);
    }

    @Transactional
    public void deleteProduct(UUID productId, UUID sellerId) {
        Product product = productRepository.findProductByIdAndSellerId(productId, sellerId)
                .orElseThrow(() -> new NotFoundProductException("판매자가 등록한 상품 조회 실패. id = " + productId));

        product.updateStatus(STOPPED);

        // 상품에 등록된 이미지 삭제
        List<S3DeletionMessage> deleteMediaMessages = productMediaService.getProductDeleteMessages(product);
        applicationEventPublisher.publishEvent(new ProductMediaDeletedEvent(deleteMediaMessages));
    }

    private void updatePosition(List<MediaPosition> mediaPositions, Product product) {
        List<ProductMedia> productMedias = getAndValidateProductMedias(product.getId(), mediaPositions);
        reorderMediaOnly(mediaPositions, productMedias);
    }

    private void addNewMediaAndUpdatePosition(List<MediaPosition> mediaPositions, List<MultipartFile> files,
                                              Product product) {
        updatePosition(mediaPositions, product);
        productMediaRepository.flush();

        addNewMediaOnly(product, files);
    }

    private Product getProductById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundProductException("상품 조회에 실패했습니다. id = " + productId));
    }

    private boolean hasMediaPositions(List<MediaPosition> mediaPositions) {
        return mediaPositions != null && !mediaPositions.isEmpty();
    }

    private boolean hasFiles(List<MultipartFile> files) {
        return files != null && !files.isEmpty();
    }

    private void addNewMediaOnly(Product product, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        // 상품에 등록된 이미지 중에서 가장 마지막 position 계산
        Integer lastPosition = productMediaRepository.findLastIdxByProductId(product.getId());
        s3UploadService.upload(product, files, lastPosition + 1);
    }

    private void reorderMediaOnly(List<MediaPosition> mediaPositions, List<ProductMedia> productMedias) {
        int tempPosition = TEMP_POSITION_START;

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
