package com.practice.commerce.domain.product.entity;

import com.practice.commerce.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "product_media",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_media_product_position",
                        columnNames = {"product_id", "position"}
                )
        }
)
public class ProductMedia extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_id",
            referencedColumnName = "id",
            columnDefinition = "BINARY(16)",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_media_product")
    )
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType mediaType;

    @Column(nullable = false)
    private String bucketName;

    @Column(nullable = false)
    private String bucketKey;

    private int position; // 0 == 대표이미지

    private Integer width;
    private Integer height;

    @Builder
    public ProductMedia(Product product, MediaType mediaType, String bucketName, String bucketKey, int position,
                        Integer width, Integer height) {
        this.product = product;
        this.mediaType = mediaType;
        this.bucketName = bucketName;
        this.bucketKey = bucketKey;
        this.position = position;
        this.width = width;
        this.height = height;
    }

    public void updatePosition(int newPosition) {
        this.position = newPosition;
    }
}
