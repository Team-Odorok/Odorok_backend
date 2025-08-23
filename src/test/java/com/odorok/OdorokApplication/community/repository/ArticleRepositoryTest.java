package com.odorok.OdorokApplication.community.repository;

import com.odorok.OdorokApplication.community.dto.request.ArticleSearchCondition;
import com.odorok.OdorokApplication.community.dto.response.ArticleDetail;
import com.odorok.OdorokApplication.community.dto.response.ArticleSummary;
import com.odorok.OdorokApplication.commons.querydsl.config.QueryDslConfig;
import com.odorok.OdorokApplication.course.repository.UserRepository;
import com.odorok.OdorokApplication.domain.User;
import com.odorok.OdorokApplication.draftDomain.Article;
import com.odorok.OdorokApplication.draftDomain.Profile;
import com.odorok.OdorokApplication.draftDomain.Tier;
import com.odorok.OdorokApplication.mypage.repository.TierRepository;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
//H2작업 설정
@ActiveProfiles("test")
@DataJpaTest
@Import(QueryDslConfig.class)
@EntityScan("com.odorok.OdorokApplication")
@EnableJpaRepositories(basePackageClasses = {ArticleRepository.class,UserRepository.class,TierRepository.class,ProfileRepository.class})
@Transactional
@ActiveProfiles("test")
class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TierRepository tierRepository;
    @Autowired
    private ProfileRepository profileRepository;

    private List<Article> list;

    @BeforeEach
    void setup() {
        User user = userRepository.save(
                User.builder().email("ssn").nickname("1q").password("11").build()
        );
        list = articleRepository.saveAll(List.of(
                new Article("첫 번째 게시글", 5, 100, 3, 0, false, user.getId()),
                new Article("두 번째 게시글", 2, 45, 1, 1, false, user.getId()),
                new Article("세 번째 게시글", 10, 300, 4, 2, false, user.getId()),
                new Article("네 번째 게시글", 0, 5, 0, 1, false, user.getId())
        ));
        Tier tier = tierRepository.save(Tier.builder().title("은장").build());
        profileRepository.save(Profile.builder().userId(user.getId()).tierId(tier.getId()).build());

    }

    @Test
    void 게시글_전체_조회_정상작동() {
        ArticleSearchCondition cond = new ArticleSearchCondition();
        //좋아요 순으로 정렬
        cond.setSort("likeCount");
        //1페이지 확인
        cond.setPageNum(1);
        //예상값
        List<String> expected = new ArrayList<>(List.of("세 번째 게시글","첫 번째 게시글","두 번째 게시글","네 번째 게시글"));
        List<ArticleSummary> articles = articleRepository.findByCondition(cond);
        List<String> result = new ArrayList<>();
        for(ArticleSummary as : articles){
            result.add(as.getTitle());
        }
        assertEquals(expected,result);
    }

    @Test
    void 검색_기능_정상작동(){
        //given
        ArticleSearchCondition cond = new ArticleSearchCondition();
        //좋아요 순으로 정렬
        cond.setSort("likeCount");
        //1페이지 확인
        cond.setPageNum(1);
        //검색할 문자
        cond.setTitle("네 번");
        //예상값
        List<String> expected = new ArrayList<>(List.of("네 번째 게시글"));
        List<ArticleSummary> articles = articleRepository.findByCondition(cond);
        List<String> result = new ArrayList<>();
        for(ArticleSummary as : articles){
            result.add(as.getTitle());
        }
        assertEquals(expected,result);
    }

    @Test
    void 세부조회_기능_정상작동(){
        Long articleId = list.get(0).getId();
        ArticleDetail detail = articleRepository.findArticleDetailById(articleId);
        String expected = "1q";
        String expectTierTitle = "은장";
        assertEquals(expected,detail.getNickName());
        assertEquals(expectTierTitle,detail.getTierTitle());
    }
}