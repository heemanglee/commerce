package com.practice.commerce.domain.product.repository;

import static com.practice.commerce.domain.product.entity.QProduct.product;

import com.practice.commerce.domain.product.entity.Product;
import com.practice.commerce.domain.product.entity.QProduct;
import com.practice.commerce.domain.product.entity.QProductCategory;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@Slf4j
@RequiredArgsConstructor
public class ProductCategoryRepositoryImpl implements ProductCategoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> searchProducts(UUID categoryId, String searchKeyword, Pageable pageable) {
        QProductCategory productCategory = QProductCategory.productCategory;
        QProduct product = QProduct.product;

        List<Product> content = queryFactory
                .select(productCategory.product)
                .from(productCategory)
                .join(productCategory.product, product)
                .where(
                        categoryIdEq(categoryId),
                        productNameLike(searchKeyword)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(productCategory.count())
                .from(productCategory)
                .join(productCategory.product, product)
                .where(
                        categoryIdEq(categoryId),
                        productNameLike(searchKeyword)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression categoryIdEq(UUID categoryId) {
        return categoryId != null ? QProductCategory.productCategory.category.id.eq(categoryId) : null;
    }

    private BooleanExpression productNameLike(String searchKeyword) {
        return (searchKeyword != null && !searchKeyword.trim().isEmpty())
                ? product.name.contains(searchKeyword)
                : null;
    }
}