package com.odorok.OdorokApplication.community.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleSearchResponse {
    private List<ArticleSummary> articles;
    private Integer endPage;
    private Long firstId;
    private Long lastId;
    private Integer firstLike;
    private Integer lastLike;
    private Integer firstView;
    private Integer lastView;
}
