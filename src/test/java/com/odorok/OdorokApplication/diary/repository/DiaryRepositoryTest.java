package com.odorok.OdorokApplication.diary.repository;

import com.odorok.OdorokApplication.commons.querydsl.config.QueryDslConfig;
import com.odorok.OdorokApplication.diary.dto.response.DiaryDetail;
import com.odorok.OdorokApplication.domain.Diary;
import com.odorok.OdorokApplication.domain.DiaryImage;
import com.odorok.OdorokApplication.domain.User;
import com.odorok.OdorokApplication.domain.VisitedCourse;
import com.odorok.OdorokApplication.infrastructures.domain.Course;
import com.odorok.OdorokApplication.infrastructures.domain.Route;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(QueryDslConfig.class)
public class DiaryRepositoryTest {

    @Autowired
    DiaryRepository diaryRepository;

    @Autowired
    EntityManager em;

    User mockUser;
    Course mockCourse;
    VisitedCourse mockVisitedCourse;
    Diary mockDiary;
    DiaryImage img1;
    DiaryImage img2;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .name("김철수")
                .nickname("철수")
                .email("abc123@gmail.com")
                .password("Qwer1234")
                .role("USER")
                .build();

        Route mockRoute = Route.builder()
                .idx("R123")
                .name("한강 자전거길")
                .lineMsg("한강을 따라 달리는 시원한 자전거 코스.")
                .overview("서울 도심을 가로지르는 한강을 따라 자전거를 타며 자연과 도시를 함께 즐길 수 있는 테마.")
                .brdDiv(true) // 자전거길인 경우 true
                .createdAt(LocalDateTime.of(2023, 8, 1, 10, 0))
                .modifiedAt(LocalDateTime.of(2023, 8, 5, 12, 0))
                .build();
        mockUser = em.merge(mockUser);
        mockRoute = em.merge(mockRoute);
        mockCourse = Course.builder()
                .themeId(1L)
                .idx("CRS001")
                .routeIdx(mockRoute.getIdx())
                .name("한강 자전거 코스")
                .distance(15.2)
                .reqTime(2.5)
                .level(2)
                .cycle(true)
                .summary("한강을 따라 달리는 시원한 자전거 길입니다.")
                .contents("서울의 중심을 가로지르며 자연과 도시를 동시에 느낄 수 있는 명품 코스입니다.")
                .travelerInfo("자전거 이용 가능, 가족 여행 적합")
                .sidoCode(1)
                .sigunguCode(1)
                .brdDiv(true)
                .createdAt(LocalDateTime.of(2025, 6, 1, 10, 0))
                .modifiedAt(LocalDateTime.of(2025, 6, 20, 12, 0))
                .reward(600) // level 2 * 300
                .build();

        mockCourse = em.merge(mockCourse);

        mockVisitedCourse = VisitedCourse.builder()
                .userId(mockUser.getId())
                .courseId(mockCourse.getId())
                .visitedAt(LocalDateTime.of(2025, 6, 25, 14, 30))  // 예시 날짜/시간
                .startCoordsId(200L)
                .endCoordsId(201L)
                .imgUrl("https://via.placeholder.com/300x200?text=VisitedCourse")
                .stars(4)
                .review("정말 좋은 코스였어요!")
                .isFinished(true)
                .build();
        mockVisitedCourse = em.merge(mockVisitedCourse);

        mockDiary = Diary.builder()
                .userId(mockUser.getId())
                .vcourseId(mockVisitedCourse.getId())
                .title("2025년 6월 25일의 일기")
                .content("오늘은 산책을 다녀왔다. 중간에 살짝 뛰었는데 죽는 줄 알았다. 운동 좀 더 해야할 것 같다. ")
                .createdAt(LocalDateTime.of(2025, 6, 25, 14, 30, 0))
                .build();

        mockDiary = em.merge(mockDiary);

        img1 = DiaryImage.builder()
                .diaryId(mockDiary.getId())
                .imgUrl("mockDiaryImg1.com")
                .build();
        img2 = DiaryImage.builder()
                .diaryId(mockDiary.getId())
                .imgUrl("mockDiaryImg2.com")
                .build();

        img1 = em.merge(img1);
        img2 = em.merge(img2);
    }

    @Test
    @Transactional
    void 일지_상세정보_조회_성공() {

        long userId = mockUser.getId();
        long diaryId = mockDiary.getId();

        DiaryDetail result = diaryRepository.findDiaryById(userId, diaryId);

        assertNotNull(result);
        assertEquals(mockDiary.getTitle(), result.getTitle());
        assertTrue(result.getContent().contains("산책을 다녀왔다"));
        assertEquals(mockCourse.getName(), result.getCourseName());
        assertEquals(mockVisitedCourse.getVisitedAt(), result.getVisitedAt());

        List<String> expectedImgs = List.of(
                img1.getImgUrl(),
                img2.getImgUrl()
        );
        List<String> actualImgs = result.getImgs();

        assertTrue(actualImgs.containsAll(expectedImgs) && expectedImgs.containsAll(actualImgs));
    }



}
