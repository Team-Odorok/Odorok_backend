package com.odorok.OdorokApplication.community.repository;

import com.odorok.OdorokApplication.community.dto.request.ArticleSearchCondition;
import com.odorok.OdorokApplication.community.dto.response.ArticleDetail;
import com.odorok.OdorokApplication.community.dto.response.ArticleSearchResponse;
import com.odorok.OdorokApplication.community.dto.response.ArticleSummary;
import com.odorok.OdorokApplication.commons.querydsl.config.QueryDslConfig;
import com.odorok.OdorokApplication.course.repository.UserRepository;
import com.odorok.OdorokApplication.domain.User;
import com.odorok.OdorokApplication.draftDomain.Article;
import com.odorok.OdorokApplication.draftDomain.Profile;
import com.odorok.OdorokApplication.draftDomain.Tier;
import com.odorok.OdorokApplication.mypage.repository.TierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@Import(QueryDslConfig.class)
@EntityScan("com.odorok.OdorokApplication")
@EnableJpaRepositories(basePackageClasses = {ArticleRepository.class, UserRepository.class, TierRepository.class, ProfileRepository.class})
@Transactional
@TestPropertySource(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ArticleRepositoryTest {

    @Autowired private ArticleRepository articleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private TierRepository tierRepository;
    @Autowired private ProfileRepository profileRepository;

    private List<Article> list;
    private Long seedUserId;

    @BeforeEach
    void setup() {
        // 유저/프로필/티어
        User user = userRepository.save(
                User.builder().email("user@test.com").nickname("nick").password("pw").build()
        );
        seedUserId = user.getId();
        Tier tier = tierRepository.save(Tier.builder().title("은장").build());
        profileRepository.save(Profile.builder().userId(seedUserId).tierId(tier.getId()).build());

        // 기본 시드: 15개 (pageSize=3 → 5페이지)
        List<Article> bulk = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            bulk.add(Article.builder()
                    .title("post-" + i)
                    .likeCount((i % 5 == 0) ? 100 - i : 100 - (i / 2))
                    .viewCount(i * 10)
                    .commentCount(i % 3)
                    .diseaseId((i % 2 == 0) ? 1 : null) // 짝수=1, 홀수=null
                    .notice(false)
                    .userId(seedUserId)
                    .createdAt(LocalDateTime.now().minusDays(100 - i))
                    .build());
        }
        list = articleRepository.saveAll(bulk);
    }

    // ---------- 헬퍼들 ----------
    private ArticleSearchResponse find(ArticleSearchCondition c) {
        return articleRepository.findByCondition(c);
    }

    /** 초기 첫페이지(커서 없음) */
    private ArticleSearchCondition firstPageCond(String sort, Integer diseaseId, int pageSize) {
        ArticleSearchCondition c = new ArticleSearchCondition();
        c.setSort(sort);
        c.setDiseaseId(diseaseId);
        c.setPageSize(pageSize);
        c.setCurrentPageNum(1);
        c.setPageNum(1);
        c.setFirstId(null);
        c.setLastId(null);
        return c;
    }

    /** 기본값 헬퍼(여러 테스트에서 사용) */
    private ArticleSearchCondition baseCond() {
        return firstPageCond("createdAt", null, 3);
    }

    /** 오래된 쪽 점프(예: 1→3): lastId 사용 */
    private ArticleSearchCondition jumpForwardCond(int from, int to, Long lastId,
                                                   String sort, Integer diseaseId, int pageSize) {
        ArticleSearchCondition c = new ArticleSearchCondition();
        c.setSort(sort);
        c.setDiseaseId(diseaseId);
        c.setPageSize(pageSize);
        c.setCurrentPageNum(from);
        c.setPageNum(to);
        c.setLastId(lastId);
        c.setFirstId(null);
        return c;
    }

    /** 최신 쪽 점프(예: 3→1): firstId 사용 */
    private ArticleSearchCondition jumpBackwardCond(int from, int to, Long firstId,
                                                    String sort, Integer diseaseId, int pageSize) {
        ArticleSearchCondition c = new ArticleSearchCondition();
        c.setSort(sort);
        c.setDiseaseId(diseaseId);
        c.setPageSize(pageSize);
        c.setCurrentPageNum(from);
        c.setPageNum(to);
        c.setFirstId(firstId);
        c.setLastId(null);
        return c;
    }

    // n개로 재시드(특정 케이스용)
    private void reseed(int n, Integer fixedDiseaseId) {
        articleRepository.deleteAll();
        List<Article> bulk = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            bulk.add(Article.builder()
                    .title("post-" + i)
                    .likeCount((i % 5 == 0) ? 100 - i : 100 - (i / 2))
                    .viewCount(i * 10)
                    .commentCount(i % 3)
                    .diseaseId(fixedDiseaseId != null ? fixedDiseaseId : null)
                    .notice(false)
                    .userId(seedUserId)
                    .createdAt(LocalDateTime.now().minusDays(100 - i))
                    .build());
        }
        list = articleRepository.saveAll(bulk);
    }


    @Test
    void 세부조회_기능_정상작동() {
        Long articleId = list.get(0).getId();
        ArticleDetail detail = articleRepository.findArticleDetailById(articleId);
        assertEquals("nick", detail.getNickName());
        assertEquals("은장", detail.getTierTitle());
    }

    @Test
    void 최초진입_createdAt_DESC_id_DESC_정렬확인() {
        ArticleSearchCondition c = baseCond();
        c.setSort("createdAt");
        ArticleSearchResponse r = find(c);

        assertEquals(3, r.getArticles().size());
        List<Long> ids = r.getArticles().stream().map(ArticleSummary::getId).toList();
        assertTrue(ids.get(0) > ids.get(1) && ids.get(1) > ids.get(2));
        assertNotNull(r.getFirstId());
        assertNotNull(r.getLastId());
        assertTrue(r.getEndPage() >= 1 && r.getEndPage() <= 10);
    }

    @Test
    void 최초진입_likeCount_DESC_정렬확인_diseaseId_null() {
        ArticleSearchCondition c = baseCond();
        c.setSort("likeCount");
        ArticleSearchResponse r = find(c);

        assertEquals(3, r.getArticles().size());
        int l0 = r.getArticles().get(0).getLikeCount();
        int l1 = r.getArticles().get(1).getLikeCount();
        int l2 = r.getArticles().get(2).getLikeCount();
        assertTrue(l0 >= l1 && l1 >= l2);
    }

    @Test
    void diseaseId_필터_동작확인() {
        ArticleSearchCondition c = baseCond();
        c.setDiseaseId(1);
        ArticleSearchResponse r = find(c);
        assertEquals(3, r.getArticles().size());
        // (필요하면 DTO에 diseaseId 노출해 엄밀 검증)
    }

    @Test
    void likeCount_1에서3_점프_부분페이지_허용() {
        int ps = 3;
        // 1페이지(초기)
        ArticleSearchResponse r1 = find(firstPageCond("likeCount", null, ps));
        // 1→3 (오래된쪽, lastId)
        ArticleSearchResponse r3 = find(jumpForwardCond(1, 3, r1.getLastId(), "likeCount", null, ps));
        // 마지막 페이지는 덜 찰 수 있음(0~ps)
        assertTrue(r3.getArticles().size() >= 0 && r3.getArticles().size() <= ps);
        // 정렬 유지 검사(내림차순, 동점시 id DESC)
        for (int i = 1; i < r3.getArticles().size(); i++) {
            var prev = r3.getArticles().get(i-1);
            var cur  = r3.getArticles().get(i);
            assertTrue(prev.getLikeCount() > cur.getLikeCount()
                    || (prev.getLikeCount().equals(cur.getLikeCount()) && prev.getId() > cur.getId()));
        }
    }

    @Test
    void likeCount_3에서1_점프_부분페이지_허용() {
        int ps = 3;
        ArticleSearchResponse r1 = find(firstPageCond("likeCount", null, ps));
        ArticleSearchResponse r3 = find(jumpForwardCond(1, 3, r1.getLastId(), "likeCount", null, ps));
        // 3→1 (최신쪽, firstId)
        ArticleSearchResponse rBack = find(jumpBackwardCond(3, 1, r3.getFirstId(), "likeCount", null, ps));
        assertTrue(rBack.getArticles().size() >= 0 && rBack.getArticles().size() <= ps);
    }

    @Test
    void viewCount_DESC_정렬과_점프_대략적_연속성() {
        int ps = 3;
        ArticleSearchResponse r1 = find(firstPageCond("viewCount", null, ps));

        ArticleSearchCondition c = jumpForwardCond(1, 2, r1.getLastId(), "viewCount", null, ps);
        c.setLastView(r1.getLastView());

        ArticleSearchResponse r2 = find(c);

        assertEquals(3, r2.getArticles().size());

        // 경계(사전식) 검증: r2의 모든 (view,id)는 r1의 last(view,id)보다 작아야 함
        int lastView = r1.getLastView();
        long lastId  = r1.getLastId();
        assertTrue(r2.getArticles().stream().allMatch(s ->
                s.getViewCount() < lastView ||
                        (s.getViewCount() == lastView && s.getId() < lastId)
        ));
    }

    @Test
    void endPage_블록끝_계산_동작확인() {
        ArticleSearchResponse r1 = find(firstPageCond("createdAt", null, 3));
        ArticleSearchResponse r3 = find(jumpForwardCond(1, 3, r1.getLastId(), "createdAt", null, 3));
        ArticleSearchCondition measure = firstPageCond("createdAt", null, 3);
        measure.setCurrentPageNum(3);
        measure.setPageNum(3);
        measure.setLastId(r3.getLastId());
        int endPage = find(measure).getEndPage();
        assertTrue(endPage >= 3 && endPage <= 10);
    }

    @Test
    void 최초진입_null_경계_동작확인() {
        ArticleSearchResponse r = find(firstPageCond("createdAt", null, 3));
        assertEquals(3, r.getArticles().size());
        // endPage: lastId 없으면 current 반환(보수적) → current=1
        ArticleSearchCondition measure = firstPageCond("createdAt", null, 3);
        measure.setCurrentPageNum(1);
        measure.setPageNum(1);
        measure.setLastId(null);
        assertEquals(1, find(measure).getEndPage());
    }

    // ★ 추가: 7개면 end=3, 3페이지는 1개
    @Test
    void 데이터_7개면_endPage는3_3페이지는1개() {
        reseed(7, null); // 전체 7개, pageSize=3 → 1p=3, 2p=3, 3p=1

        int ps = 3;
        // 1페이지(초기)
        ArticleSearchResponse r1 = find(firstPageCond("createdAt", null, ps));
        assertEquals(3, r1.getArticles().size());

        // endPage 측정(current=1, lastId 사용)
        ArticleSearchCondition probe = firstPageCond("createdAt", null, ps);
        probe.setCurrentPageNum(1);
        probe.setPageNum(1);
        probe.setLastId(r1.getLastId());
        assertEquals(3, find(probe).getEndPage());

        // 1→3
        ArticleSearchResponse r3 = find(jumpForwardCond(1, 3, r1.getLastId(), "createdAt", null, ps));
        assertEquals(1, r3.getArticles().size());
    }
    @Test
    void view_1에서2_점프_튜플커서_경계검증() {
        int ps = 3;
        var r1 = find(firstPageCond("viewCount", null, ps));

        var c = jumpForwardCond(1, 2, r1.getLastId(), "viewCount", null, ps);
        c.setLastView(r1.getLastView()); // ★ 반드시 함께
        var r2 = find(c);

        assertTrue(r2.getArticles().size() >= 0 && r2.getArticles().size() <= ps);

        int lastView = r1.getLastView();
        long lastId  = r1.getLastId();
        assertTrue(r2.getArticles().stream().allMatch(s ->
                s.getViewCount() < lastView ||
                        (s.getViewCount() == lastView && s.getId() < lastId)
        ));

        var ids1 = r1.getArticles().stream().map(ArticleSummary::getId).collect(Collectors.toSet());
        var ids2 = r2.getArticles().stream().map(ArticleSummary::getId).collect(Collectors.toSet());
        ids1.retainAll(ids2);
        assertTrue(ids1.isEmpty()); // 중복 없음
    }
    @Test
    void like_3에서1_점프_튜플커서_경계검증() {
        int ps = 3;
        var r1 = find(firstPageCond("likeCount", null, ps));
        var c2 = jumpForwardCond(1, 3, r1.getLastId(), "likeCount", null, ps);
        c2.setLastLike(r1.getLastLike());
        var r3 = find(c2);

        var back = jumpBackwardCond(3, 1, r3.getFirstId(), "likeCount", null, ps);
        back.setFirstLike(r3.getFirstLike()); // ★ 반드시 함께
        var rBack = find(back);

        int firstLike = r3.getFirstLike();
        long firstId  = r3.getFirstId();
        assertTrue(rBack.getArticles().stream().allMatch(s ->
                s.getLikeCount() > firstLike ||
                        (s.getLikeCount() == firstLike && s.getId() > firstId)
        ));
    }
    @Test
    void view_타이경계_정확성() {
        // 동일 viewCount가 여러 개 생기도록 시드
        reseed(20, 1); // 내부 시드 로직이 같은 view 생성되도록 되어 있으면 그대로 사용
        int ps = 5;

        var r1 = find(firstPageCond("viewCount", 1, ps));
        var c = jumpForwardCond(1, 2, r1.getLastId(), "viewCount", 1, ps);
        c.setLastView(r1.getLastView());
        var r2 = find(c);

        int lastView = r1.getLastView();
        long lastId  = r1.getLastId();
        // r2는 (view < lastView) or (view == lastView and id < lastId)만 포함
        assertTrue(r2.getArticles().stream().allMatch(s ->
                s.getViewCount() < lastView ||
                        (s.getViewCount() == lastView && s.getId() < lastId)
        ));
    }
    @Test
    void createdAt_왕복_정렬과_중복없음() {
        int ps = 3;
        var r1 = find(firstPageCond("createdAt", null, ps));

        var r2 = find(jumpForwardCond(1, 2, r1.getLastId(), "createdAt", null, ps));
        // r2는 id DESC 정렬 유지
        for (int i = 1; i < r2.getArticles().size(); i++) {
            assertTrue(r2.getArticles().get(i-1).getId() > r2.getArticles().get(i).getId());
        }

        var back = jumpBackwardCond(2, 1, r2.getFirstId(), "createdAt", null, ps);
        var rBack = find(back);

        var ids1 = r1.getArticles().stream().map(ArticleSummary::getId).toList();
        var idsBack = rBack.getArticles().stream().map(ArticleSummary::getId).toList();
        assertEquals(ids1, idsBack); // 완전히 동일하게 복귀
    }
    @Test
    void 정렬변경_커서리셋() {
        int ps = 3;
        var rView = find(firstPageCond("viewCount", null, ps));
        // 바로 likeCount로 요청(이때 lastView/lastId는 무시되어야 함)
        var rLike = find(firstPageCond("likeCount", null, ps));

        // 단조성만 확인(같을 필요는 없음)
        for (int i = 1; i < rLike.getArticles().size(); i++) {
            var p = rLike.getArticles().get(i-1);
            var q = rLike.getArticles().get(i);
            assertTrue(p.getLikeCount() > q.getLikeCount() ||
                    (p.getLikeCount().equals(q.getLikeCount()) && p.getId() > q.getId()));
        }
    }





}
