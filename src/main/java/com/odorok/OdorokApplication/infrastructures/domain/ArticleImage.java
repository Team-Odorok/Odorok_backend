package com.odorok.OdorokApplication.infrastructures.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "article_images")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "img_url")
    private String imgUrl;

    @Column(name = "article_id")
    private Long articleId;
}
