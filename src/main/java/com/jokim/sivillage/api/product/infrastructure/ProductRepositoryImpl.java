package com.jokim.sivillage.api.product.infrastructure;

import com.jokim.sivillage.api.brand.domain.QBrand;
import com.jokim.sivillage.api.bridge.domain.QBrandProductList;
import com.jokim.sivillage.api.bridge.domain.QProductMediaList;
import com.jokim.sivillage.api.hashtag.domain.QHashtag;
import com.jokim.sivillage.api.hashtag.domain.QProductHashtag;
import com.jokim.sivillage.api.media.domain.QMedia;
import com.jokim.sivillage.api.product.domain.Product;
//import com.querydsl.jpa.impl.JPAQueryFactory;
import com.jokim.sivillage.api.product.domain.QProduct;
import com.jokim.sivillage.api.product.domain.QProductOption;
import com.jokim.sivillage.api.product.domain.option.QColor;
import com.jokim.sivillage.api.product.domain.option.QEtc;
import com.jokim.sivillage.api.product.domain.option.QSize;
import com.jokim.sivillage.api.product.dto.out.ProductResponseDto;
import com.jokim.sivillage.api.hashtag.vo.HashtagResponseVo;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
@Slf4j
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Product> findFilteredProduct(Long sizeId, Long colorId, Long etcId) {

        QProduct product = QProduct.product;
        QProductOption productOption = QProductOption.productOption;
        QSize size = QSize.size;
        QColor color = QColor.color;
        QEtc etc = QEtc.etc;

        log.info("productOption.size.id {}", productOption.size.id);

        return jpaQueryFactory.selectFrom(product)
            .join(productOption).on(product.productCode.eq(productOption.productCode))
            .where(productOption.size.id.eq(sizeId),
                productOption.color.id.eq(colorId),
                productOption.etc.id.eq(etcId))
            .fetch();
    }

    @Override
    public ProductResponseDto findProductByProductCode(String productCode) {
        QProduct product = QProduct.product;
        QBrandProductList brandProductList = QBrandProductList.brandProductList;
        QBrand brand = QBrand.brand;
        QProductHashtag productHashtag = QProductHashtag.productHashtag;
        QHashtag hashtag = QHashtag.hashtag;
        QProductMediaList productMediaList = QProductMediaList.productMediaList;
        QMedia media = QMedia.media;

        ProductResponseDto productResponseDto = jpaQueryFactory
            .select(Projections.bean(
                ProductResponseDto.class,
                product.productCode.as("productCode"),
                media.url.as("imageUrl"),
                brand.mainName.as("brandName"),
                Expressions.numberTemplate(Integer.class, "((1 - ({0}/{1}))*100) ",
                    product.discountPrice,
                    product.standardPrice).as("discountRate"),
                product.productName.as("productName"),
                product.isOnSale.as("isOnSale"),
                product.standardPrice.as("price"),
                product.discountPrice.as("amount"),
                product.detail.as("detail")

            ))
            .from(product)
            .leftJoin(productMediaList).on(product.productCode.eq(
                productMediaList.productCode))  // productMediaList와 product 조인
            .leftJoin(media)
            .on(productMediaList.mediaCode.eq(media.mediaCode))  // productMediaList와 media 조인
            .leftJoin(brandProductList).on(product.productCode.eq(brandProductList.productCode))
            .leftJoin(brand).on(brandProductList.brandCode.eq(brand.brandCode))
            .where(product.productCode.eq(productCode))
            .fetchOne();

        log.info("ProductResponseDto {} in repository", productResponseDto.toString());
        // Hashtag 리스트 쿼리
        // todo null 값 들어오는 오류
        List<HashtagResponseVo> hashtagResponseVos = jpaQueryFactory
            .select(Projections.bean(
                HashtagResponseVo.class,
                hashtag.id.as("hashtagId"),
                hashtag.value.as("value")
            ))
            .from(productHashtag)
            .leftJoin(hashtag).on(productHashtag.hashtag.id.eq(hashtag.id))
            .where(productHashtag.productCode.eq(productCode))
            .fetch();
        hashtagResponseVos.forEach(vo -> log.info("HashtagResponseVo: {}", vo));
        log.info("hashtagResponseVos {}", hashtagResponseVos.toString());

        // DTO에 Hashtag 리스트 추가
        if (productResponseDto != null) {
            productResponseDto.setHashTag(hashtagResponseVos);
        }
        log.info("ProductResponseDto {} in repository", productResponseDto.toString());
        return productResponseDto;
    }
}
