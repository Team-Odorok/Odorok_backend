package com.odorok.OdorokApplication.infrastructures.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity(name = "articles")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @Column(name = "like_count")
    private Integer likeCount;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "comment_count")
    private Integer commentCount;

    @Column(name = "board_type")
    private Integer boardType;

    @Column(name = "notice")
    private Boolean notice;

    @Column(name = "start")
    private Integer start;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "vcourse_id")
    private Long vcourseId;

    @Column(name = "disease_id")
    private Integer diseaseId;
}
