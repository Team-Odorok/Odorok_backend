package com.odorok.OdorokApplication.visitedCourse.repository;

import com.odorok.OdorokApplication.commons.querydsl.config.QueryDslConfig;
import com.odorok.OdorokApplication.diary.dto.gpt.VisitedAdditionalAttraction;
import com.odorok.OdorokApplication.diary.dto.gpt.VisitedCourseAndAttraction;
import com.odorok.OdorokApplication.domain.VisitedAttraction;
import com.odorok.OdorokApplication.domain.VisitedCourse;
import com.odorok.OdorokApplication.draftDomain.Attraction;
import com.odorok.OdorokApplication.infrastructures.domain.Course;
import com.odorok.OdorokApplication.diary.repository.VisitedCourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(QueryDslConfig.class)
public class VisitedCourseRepositoryTest {
    @Autowired
    VisitedCourseRepository visitedCourseRepository;

    @Autowired
    private TestEntityManager em;

    private Long userId;
    private Long visitedCourseId;
    private Course course;
    private Attraction attraction1;
    private Attraction attraction2;

    @BeforeEach
    void setUp() {
        userId = 1L;
        course = Course.builder()
                .themeId(null)
                .idx("T_CRS_MNG_TEST_" + UUID.randomUUID())
                .routeIdx("T_ROUTE_MNG0000000001")
                .name("남파랑길 1코스")
                .distance(19.0)
                .reqTime(420.0)
                .level(2)
                .cycle(false)
                .summary("갈맷길 3-1구간, 3-2구간과 중첩되는 구간으로서 해파랑길 시종점인 오륙도 해맞이 공원에서부터 부산 중구 부산대교까지 이어지는 구간이다.  신선이 노닐던 신선대 및 부산항의 역동적인 파노라마을 만끽할 수 있는 구간이며 세계에서 하나뿐인 UN기념공원 및 부산박물관, 영화 “친구”로 유명한 부산 일대의 명소를 함께 체험할 수 있다.  아름다운 해안경관과 우리나라 제1의 항구도시 부산의 매력을 느낄 수 있는 구간이다.")
                .contents("- 해파랑길 시종점인 오륙도 해맞이 공원에서부터 부산 중구 부산대교까지 이어지는 구간<br>- 신선이 노닐던 신선대 및 부산항의 역동적인 파노라마를 만끽할 수 있는 구간으로 세계에서 하나뿐인 UN기념공원 및 부산박물관, 영화 “친구”로 유명한 부산 일대의 명소를 함께 체험할 수 있는 코스<br>- 아름다운 해안경관과 우리나라 제1의 항구도시 부산의 매력을 느낄 수 있는 구간<br>- 부산 갈맷길 3-1, 3-2코스가 중첩됨")
                .travelerInfo("- 시점 : 오륙도해맞이공원 (부산 남구 오륙도로 137) <br>교통편) 경성대부경대역 24번 버스, 오륙도스카이워크 하차<br>- 종점 : 부산역 (부산 동구 중앙대로 210) <br>교통편) 부산지하철 1호선 부산역<br>- 도시 구간을 지나는 코스이므로 화장실, 매점 등 편의시설 이용이 용이함 <br>- 재한유엔기념공원 운영시간 10월~4월 09:00~17:00 / 5월~9월 09:00~18:00 <br>* 재한유엔기념공원 입장시간은 운영시간 30분 전까지 가능합니다. 운영시간 외에는 우회하여 이용하시기 바랍니다.")
                .sidoCode(6)
                .sigunguCode(4)
                .brdDiv(false)
                .createdAt(LocalDateTime.of(2020, 2, 16, 2, 41, 53))
                .modifiedAt(LocalDateTime.of(2025, 4, 23, 6, 52, 30))
                .reward(600)
                .build();

        em.persist(course);

        attraction1 = Attraction.builder()
                .id(9999999L)
                .contentId(1000001)
                .title("해운대")
                .firstImage1("https://example.com/image1.jpg")
                .firstImage2("https://example.com/image2.jpg")
                .mapLevel(6)
                .latitude(35.1587)
                .longitude(129.1604)
                .tel("051-123-4567")
                .addr1("부산 해운대구 해운대로 264")
                .addr2("해운대해변로")
                .homepage("https://haeundae.go.kr")
                .overview("부산을 대표하는 해변 관광지로, 여름철 해수욕과 다양한 행사로 유명합니다.")
                .contentTypeId(12) // 예: 관광지
                .sidoCode(6)       // 부산
                .sigunguCode(2)    // 해운대구
                .build();
        attraction2 = Attraction.builder()
                .id(9999998L)
                .contentId(1000002)
                .title("광안리")
                .firstImage1("https://example.com/gwangalli1.jpg")
                .firstImage2("https://example.com/gwangalli2.jpg")
                .mapLevel(6)
                .latitude(35.1533)
                .longitude(129.1187)
                .tel("051-987-6543")
                .addr1("부산 수영구 광안해변로 219")
                .addr2("광안리 해변")
                .homepage("https://gwangalli.co.kr")
                .overview("광안대교 야경으로 유명한 부산의 대표 해수욕장 중 하나입니다.")
                .contentTypeId(12)
                .sidoCode(6)
                .sigunguCode(3) // 수영구
                .build();
        em.merge(attraction1);
        em.merge(attraction2);

        VisitedCourse visitedCourse = VisitedCourse.builder()
                .courseId(course.getId())
                .visitedAt(LocalDateTime.of(2025, 5, 30, 12, 0, 0))
                .userId(userId)
                .startCoordsId(1L)
                .endCoordsId(1L)
                .imgUrl("image.png")
                .stars(1)
                .review("리뷰 예시")
                .isFinished(true)
                .build();
        em.persist(visitedCourse);
        visitedCourseId = visitedCourse.getId();

        VisitedAttraction visitedAttraction1 = VisitedAttraction.builder()
                .vcourseId(visitedCourseId)
                .attractionId(attraction1.getId())
                .build();
        VisitedAttraction visitedAttraction2 = VisitedAttraction.builder()
                .vcourseId(visitedCourseId)
                .attractionId(attraction2.getId())
                .build();
        em.persist(visitedAttraction1);
        em.persist(visitedAttraction2);

        em.flush();
        em.clear();
    }

    @Test
    void 방문코스Id로_명소_조회_성공() {
        List<VisitedAdditionalAttraction> result = visitedCourseRepository.findVisitedAttractionByVisitedCourseId(userId, visitedCourseId);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(a -> a.getTitle().equals("해운대")));
        assertTrue(result.stream().anyMatch(a -> a.getTitle().equals("광안리")));
        assertTrue(result.stream().anyMatch(a -> a.getAddress().equals(attraction1.getAddr1())));
        assertTrue(result.stream().anyMatch(a -> a.getAddress().equals(attraction2.getAddr1())));
    }

    @Test
    void 방문코스Id로_코스_명소_통합조회_성공() {
        VisitedCourseAndAttraction result = visitedCourseRepository.findCourseAndAttractionsByVisitedCourseId(userId, visitedCourseId);

        assertNotNull(result);
        assertEquals(course.getName(), result.getCourseName());
        assertEquals(course.getSummary(), result.getCourseSummary());
        assertEquals(2, result.getVisitedAttractions().size());
    }
}
