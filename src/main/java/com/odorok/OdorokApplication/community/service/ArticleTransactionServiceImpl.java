package com.odorok.OdorokApplication.community.service;

import com.odorok.OdorokApplication.community.dto.request.ArticleUpdateRequest;
import com.odorok.OdorokApplication.community.repository.ArticleRepository;
import com.odorok.OdorokApplication.community.repository.PointHistoryRepository;
import com.odorok.OdorokApplication.community.repository.ProfileRepository;
import com.odorok.OdorokApplication.domain.PointHistory;
import com.odorok.OdorokApplication.draftDomain.Article;
import com.odorok.OdorokApplication.draftDomain.Profile;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ArticleTransactionServiceImpl implements ArticleTransactionService{

    private final ArticleRepository articleRepository;
    private final ArticleImageService articleImageService;
    private final ProfileRepository profileRepository;
    private final PointHistoryRepository pointHistoryRepository;
    @Override
    @Transactional
    public void insertArticleTransactional(Article article, List<String> urls,Long userId){
        articleRepository.save(article);
        Long articleId = article.getId();
        //url리스트 db작업
        if(urls!=null){
            articleImageService.insertArticleImageUrl(articleId,urls);
        }
        //유저 프로필 조회
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저 프로필 없음"));
        profile.setMileage(profile.getMileage()+30);
        profile.setActivityPoint(profile.getActivityPoint()+30);
        //점수 이력 추가
//        PointHistory history = PointHistory.builder().articleId(articleId).userId(userId).point(30).build();
//        pointHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public List<String> updateArticleInfo(ArticleUpdateRequest request, List<String> newUrlList,Long articleId) {
        //트랜잭션 작업으로 빼자
        //기존 url db에서 삭제(리턴되어야 함)
        List<String> list = articleImageService.deleteArticleImageUrl(articleId);
        //새로운 url db삽입
        if(newUrlList!=null){
            articleImageService.insertArticleImageUrl(articleId,newUrlList);
        }
        //텍스트 변경
        Article article = articleRepository.getById(articleId);
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setBoardType(request.getBoardType());
        article.setNotice(request.getNotice());
        article.setDiseaseId(request.getDiseaseId());
        article.setCourseId(request.getCourseId());
        //트랜잭션 종료
        return list;
    }
}
