package com.odorok.OdorokApplication.community.dto.response;

import com.odorok.OdorokApplication.draftDomain.Article;
import com.querydsl.core.annotations.QueryProjection;
import jakarta.persistence.Column;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Setter
@Builder
public class ArticleDetail {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private Integer likeCount;
    private Integer viewCount;
    private Integer commentCount;
    private Boolean notice;
    private Long userId;
    private String nickName;
    private String tierTitle;
    private Boolean isLikedByUser;

    public ArticleDetail(Long id, String title, String content, LocalDateTime createdAt, Integer likeCount, Integer viewCount, Integer commentCount, Boolean notice, Long userId, String nickName, String tierTitle) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.likeCount = likeCount;
        this.viewCount = viewCount;
        this.commentCount = commentCount;
        this.notice = notice;
        this.userId = userId;
        this.nickName = nickName;
        this.tierTitle = tierTitle;
    }
}
