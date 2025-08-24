package com.odorok.OdorokApplication.community.repository;

import com.odorok.OdorokApplication.community.dto.request.ArticleSearchCondition;
import com.odorok.OdorokApplication.community.dto.response.ArticleDetail;
import com.odorok.OdorokApplication.community.dto.response.ArticleSearchResponse;
import com.odorok.OdorokApplication.community.dto.response.ArticleSummary;

import java.util.List;

public interface ArticleRepositoryCustom {
    ArticleSearchResponse findByCondition(ArticleSearchCondition cond);
    ArticleDetail findArticleDetailById(Long articleId);
}
