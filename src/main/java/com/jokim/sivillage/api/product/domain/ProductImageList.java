package com.jokim.sivillage.api.product.domain;

import com.jokim.sivillage.api.media.domain.Media;
import jakarta.persistence.*;

@Entity
public class ProductImageList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "image_id")
    private Media image;

    @Column(nullable = false)
    private String isThumbnail;


}
