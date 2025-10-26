package com.practice.commerce.domain.product.entity;

import com.practice.commerce.domain.common.BaseEntity;
import com.practice.commerce.domain.user.entity.User;
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
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_seller_name",
                        columnNames = {"seller_id", "name"}
                )
        }
)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, length = 25)
    @Size(max = 25)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "seller_id",
            referencedColumnName = "id",
            columnDefinition = "BINARY(16)",
            foreignKey = @ForeignKey(name = "fk_product_seller")
    )
    private User seller;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    private Instant deletedAt = null;

    @Builder
    public Product(String name, String description, User seller, ProductStatus status) {
        this.name = name;
        this.description = description;
        this.seller = seller;
        this.status = status;
    }
}