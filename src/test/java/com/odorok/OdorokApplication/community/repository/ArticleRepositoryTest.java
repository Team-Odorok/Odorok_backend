package com.odorok.OdorokApplication.community.repository;

import com.odorok.OdorokApplication.community.dto.request.ArticleSearchCondition;
import com.odorok.OdorokApplication.community.dto.response.ArticleSummary;
import com.odorok.OdorokApplication.commons.querydsl.config.QueryDslConfig;
import com.odorok.OdorokApplication.domain.User;
import com.odorok.OdorokApplication.draftDomain.Article;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(QueryDslConfig.class)
@EntityScan(basePackageClasses = {User.class, Article.class})
@EnableJpaRepositories(basePackageClasses = ArticleRepository.class)
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;

    @BeforeEach
    void setup() {
        articleRepository.saveAll(List.of(
                new Article("첫 번째 게시글", 5, 100, 3, 0, false, 1L),
                new Article("두 번째 게시글", 2, 45, 1, 1, false, 2L),
                new Article("세 번째 게시글", 10, 300, 4, 2, false, 3L),
                new Article("네 번째 게시글", 0, 5, 0, 1, false, 4L),
                new Article("다섯 번째 게시글", 3, 80, 2, 0, true, 5L),
                new Article("여섯 번째 게시글", 7, 120, 5, 2, false, 6L),
                new Article("일곱 번째 게시글", 1, 10, 0, 1, false, 1L),
                new Article("여덟 번째 게시글", 6, 150, 6, 0, false, 2L),
                new Article("아홉 번째 게시글", 0, 20, 0, 2, true, 3L),
                new Article("열 번째 게시글", 4, 60, 1, 1, false, 4L)
        ));
    }

    @Test
    void 게시글_전체_조회_정상작동() {
        ArticleSearchCondition cond = new ArticleSearchCondition();
        //여정경험담 <-1이라고 가정
        cond.setCategory(1);
        //좋아요 순으로 정렬
        cond.setSort("likeCount");
        //1페이지 확인
        cond.setPageNum(1);
        //예상값
        List<String> expected = new ArrayList<>(List.of("열 번째 게시글","두 번째 게시글","일곱 번째 게시글","네 번째 게시글"));
        List<ArticleSummary> articles = articleRepository.findByCondition(cond);
        List<String> result = new ArrayList<>();
        for(ArticleSummary as : articles){
            result.add(as.getTitle());
        }
        assertEquals(expected,result);
    }
}