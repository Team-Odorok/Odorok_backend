package com.odorok.OdorokApplication.community.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ArticleSearchCondition {
    private Integer pageNum;
    private Integer currentPageNum;
    private String sort;
    private Long firstId;
    private Long lastId;
    private Integer diseaseId;
    private Integer pageSize;
    private Integer firstLike;
    private Integer lastLike;
    private Integer firstView;
    private Integer lastView;
}