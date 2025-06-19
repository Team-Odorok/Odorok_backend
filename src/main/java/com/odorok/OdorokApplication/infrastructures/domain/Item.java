package com.odorok.OdorokApplication.infrastructures.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "items")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "price")
    private Integer price;

    @Column(name = "category")
    private Integer category;

    @Column(name = "on_sale")
    private Boolean onSale;

    @Column(name = "img_url")
    private String imgUrl;

    @Column(name = "overview")
    private String overview;

    @Column(name = "req_tier")
    private Long reqTier;
}
