package com.odorok.OdorokApplication.community.service;

import com.odorok.OdorokApplication.community.dto.request.ArticleSearchCondition;
import com.odorok.OdorokApplication.community.dto.request.ArticleRegistRequest;
import com.odorok.OdorokApplication.community.dto.request.ArticleUpdateRequest;
import com.odorok.OdorokApplication.community.dto.request.CommentRegistRequest;
import com.odorok.OdorokApplication.community.dto.response.ArticleDetail;
import com.odorok.OdorokApplication.community.dto.response.ArticleSearchResponse;
import com.odorok.OdorokApplication.community.dto.response.ArticleSummary;
import com.odorok.OdorokApplication.community.dto.response.CommentSummary;
import com.odorok.OdorokApplication.draftDomain.Article;
import com.odorok.OdorokApplication.draftDomain.Disease;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ArticleService {
    public ArticleSearchResponse findByCondition(ArticleSearchCondition condition);
    public void insertArticle(ArticleRegistRequest request, List<MultipartFile> images, Long userId);
    public ArticleDetail findByArticleId(Long articleId);
    public void deleteArticle(Long articleId);
    public void updateArticle(ArticleUpdateRequest request, List<MultipartFile> images, Long articleId,Long userId);
    public void updateLike(Long articleId,Long userId);
    public List<CommentSummary> findCommentsByArticleId(Long articleId);
    public void registComment(Long articleId,CommentRegistRequest request,Long userId);
    public List<Disease> findAllDisease();
}
