package com.odorok.OdorokApplication.community.service;

import com.odorok.OdorokApplication.commons.aop.annotation.CheckArticleOwner;
import com.odorok.OdorokApplication.community.dto.request.ArticleRegistRequest;
import com.odorok.OdorokApplication.community.dto.request.ArticleSearchCondition;
import com.odorok.OdorokApplication.community.dto.request.ArticleUpdateRequest;
import com.odorok.OdorokApplication.community.dto.request.CommentRegistRequest;
import com.odorok.OdorokApplication.community.dto.response.ArticleDetail;
import com.odorok.OdorokApplication.community.dto.response.ArticleSearchResponse;
import com.odorok.OdorokApplication.community.dto.response.ArticleSummary;
import com.odorok.OdorokApplication.community.dto.response.CommentSummary;
import com.odorok.OdorokApplication.community.repository.ArticleRepository;
import com.odorok.OdorokApplication.community.repository.CommentRepository;
import com.odorok.OdorokApplication.community.repository.DiseaseRepository;
import com.odorok.OdorokApplication.community.repository.LikeRepository;
import com.odorok.OdorokApplication.domain.Comment;
import com.odorok.OdorokApplication.domain.Like;
import com.odorok.OdorokApplication.draftDomain.Article;
import com.odorok.OdorokApplication.draftDomain.Disease;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleServiceImpl implements ArticleService{
    private final ArticleImageService articleImageService;
    private final ArticleRepository articleRepository;
    private final ArticleTransactionService articleTransactionService;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final DiseaseRepository diseaseRepository;

    @Override
    public void insertArticle(ArticleRegistRequest request, List<MultipartFile> images, Long userId) {
        List<String> urls = articleImageService.insertArticleImages(userId,images);
        Article article = Article.builder().title(request.getTitle()).content(request.getContent())
                .boardType(request.getBoardType()).notice(request.getNotice())
                .diseaseId(request.getDiseaseId()).courseId(request.getCourseId()).userId(userId).build();
        try {
            articleTransactionService.insertArticleTransactional(article, urls, userId);  // 트랜잭션 메서드
        } catch (Exception e) {
            articleImageService.deleteImages(urls); // 수동 롤백
            throw e;
        }
    }

    @Override
    public ArticleSearchResponse findByCondition(ArticleSearchCondition condition) {
        //(현재 엔드페이지를 조회할 수 있도록) 바꿀것
        return articleRepository.findByCondition(condition);
    }

    @Override
    public ArticleDetail findByArticleId(Long articleId) {
        ArticleDetail articleDetail = articleRepository.findArticleDetailById(articleId);
        return articleDetail;
    }

    @Override
    @CheckArticleOwner(articleId = "articleId")
    public void deleteArticle(Long articleId) {
        articleRepository.deleteById(articleId);
    }

    @Override
    @CheckArticleOwner(articleId = "articleId")
    public void updateArticle(ArticleUpdateRequest request, List<MultipartFile> images,Long articleId,Long userId) {
        //s3에 이미지 삽입
        List<String> newUrlList = articleImageService.insertArticleImages(userId,images);
        //db트랜잭션 작업 후 이전 urlList 반환
        List<String> oldUrlList = articleTransactionService.updateArticleInfo(request,newUrlList,articleId);
        //s3에서 url이용하여 이미지 삭제
        articleImageService.deleteImages(oldUrlList);

    }

    @Override
    @Transactional
    public void updateLike(Long articleId, Long userId) {
        Like like = Like.builder().articleId(articleId).userId(userId).build();
        likeRepository.save(like);
        Article article = articleRepository.getById(articleId);
        article.setLikeCount(article.getLikeCount()+1);
    }

    @Override
    public List<CommentSummary> findCommentsByArticleId(Long articleId) {
        return commentRepository.findCommentsByArticleId(articleId);
    }

    @Override
    public void registComment(Long articleId, CommentRegistRequest request, Long userId) {
        Comment comment = Comment.builder().articleId(articleId).content(request.getContent()).userId(userId).build();
        commentRepository.save(comment);
    }
    @Override
    public List<Disease> findAllDisease() {
        return diseaseRepository.findAll();
    }

}
