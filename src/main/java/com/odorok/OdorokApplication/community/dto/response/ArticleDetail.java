package com.odorok.OdorokApplication.community.dto.response;

import com.odorok.OdorokApplication.draftDomain.Article;
import jakarta.persistence.Column;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
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
}
