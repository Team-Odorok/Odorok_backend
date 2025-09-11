package com.odorok.OdorokApplication.course.service;

import com.odorok.OdorokApplication.course.dto.process.CourseStat;
import com.odorok.OdorokApplication.course.dto.response.item.*;
import com.odorok.OdorokApplication.course.repository.CourseRepository;
import com.odorok.OdorokApplication.domain.UserDisease;
import com.odorok.OdorokApplication.domain.VisitedCourse;
import com.odorok.OdorokApplication.infrastructures.domain.Course;
import com.odorok.OdorokApplication.infrastructures.domain.PathCoord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class CourseQueryServiceImplTest {
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private RouteQueryService routeQueryService;
    @Mock
    private VisitedCourseQueryService visitedCourseQueryService;
    @Mock
    private PathCoordQueryService pathCoordQueryService;

//    @Mock
//    private DiseaseCourseStatQueryService diseaseCourseStatQueryService;
    @Mock
    private UserDiseaseQueryService userDiseaseQueryService;

    @Mock
    private Page<Course> mockPage;

    @InjectMocks
    private CourseQueryServiceImpl courseQueryService;

    private final static Long TEST_USER_ID = 1L;
    private final static Long TEST_COURSE_ID = 1L;

    private final static Long TEST_USER_2 = 2L;
    private final static Long TEST_USER_3 = 3L;

    @Test
    public void 시군구_코드로_코스_조회에_성공한다() {
        // given : Method stub
        int sidoCode = 1, sigunguCode = 1;
        Mockito.when(mockPage.getContent()).thenReturn(List.of(
                Course.builder().id(1l).name("코스1").routeIdx("GIL001").createdAt(LocalDateTime.now()).modifiedAt(LocalDateTime.now()).build(),
                Course.builder().id(2l).name("코스2").routeIdx("GIL002").createdAt(LocalDateTime.now()).modifiedAt(LocalDateTime.now()).build()
        ));
        Mockito.when(courseRepository.findBySidoCodeAndSigunguCode(Mockito.eq(sidoCode), Mockito.eq(sigunguCode), Mockito.any()))
                .thenReturn(mockPage);
        Mockito.when(routeQueryService.queryRouteNameByRouteIdx(Mockito.anyString())).thenReturn("길");
        Mockito.when(visitedCourseQueryService.checkVisitedCourse(Mockito.eq(TEST_USER_ID), Mockito.anyLong())).thenReturn(Boolean.FALSE);

        // when
        List<CourseSummary> res = courseQueryService.queryCoursesByRegion(sidoCode, sigunguCode, TEST_USER_ID, Pageable.ofSize(10));

        // then
        assertNotNull(res);
        assertNotEquals( 0, res.size());
        Mockito.verify(courseRepository, Mockito.times(1)).findBySidoCodeAndSigunguCode(Mockito.eq(sidoCode), Mockito.eq(sigunguCode), Mockito.any(Pageable.class));
        Mockito.verify(routeQueryService, Mockito.times(2)).queryRouteNameByRouteIdx(Mockito.anyString());
        Mockito.verify(visitedCourseQueryService, Mockito.times(2)).checkVisitedCourse(Mockito.eq(TEST_USER_ID), Mockito.anyLong());

    }

    @Test
    public void 전체_코스_조회에_성공한다() {
        Pageable pageable = Pageable.ofSize(10);
        Mockito.when(courseRepository.findAll(pageable)).thenReturn(mockPage);
        Mockito.when(mockPage.getContent()).thenReturn(List.of(Course.builder().id(1l).name("코스1").routeIdx("GIL001").createdAt(LocalDateTime.now()).modifiedAt(LocalDateTime.now()).build(),
                Course.builder().id(2l).name("코스2").routeIdx("GIL002").createdAt(LocalDateTime.now()).modifiedAt(LocalDateTime.now()).build()));

        List<CourseSummary> summaries = courseQueryService.queryAllCourses(TEST_USER_ID, pageable);

        assertThat(summaries).isNotNull();
        assertThat(summaries.size()).isEqualTo(2);
        Mockito.verify(courseRepository, Mockito.times(1)).findAll(pageable);
    }

    @Test
    public void 코스_상세정보_조회에_성공한다() {
        Mockito.when(courseRepository.findById(TEST_COURSE_ID))
                .thenReturn(Optional.of(Course.builder().id(TEST_COURSE_ID).name("코스1").routeIdx("GIL001").createdAt(LocalDateTime.now()).modifiedAt(LocalDateTime.now()).build()));
        Mockito.when(visitedCourseQueryService.queryAverageStars(TEST_COURSE_ID)).thenReturn(6);
        Mockito.when(visitedCourseQueryService.queryReviewCount(TEST_COURSE_ID)).thenReturn(1000L);
        Mockito.when(pathCoordQueryService.queryCoursePathCoords(TEST_COURSE_ID)).thenReturn(List.of(new Coord(), new Coord(), new Coord()));

        CourseDetail detail = courseQueryService.queryCourseDetail(TEST_COURSE_ID);

        assertThat(detail).isNotNull();
        assertThat(detail.getAvgStars()).isEqualTo(6);
        assertThat(detail.getReviewCount()).isEqualTo(1000);
        assertThat(detail.getCoords().size()).isEqualTo(3);
    }

    @Test
    public void 최고_별점_코스_조회에_성공한다() {
        Mockito.when(visitedCourseQueryService.queryCourseStatistics()).thenReturn(List.of(
                new CourseStat(1L, 1.4, 10L, 50L),
                new CourseStat(2L, 3.7, 20L, 21L),
                new CourseStat(3L, 6.6, 80L, 80L),
                new CourseStat(4L, 2.4, 50L, 100L),
                new CourseStat(5L, 8.8, 30L, 31L),
                new CourseStat(6L, 7.1, 60L, 64L)
        ));

        Mockito.when(courseRepository.findById(Mockito.anyLong())).thenReturn(
                Optional.of(new Course())
        );

        List<RecommendedCourseSummary> summaries = courseQueryService.queryTopRatedCourses(CourseQueryService.RecommendationCriteria.STARS);

        System.out.println(summaries);
        assertThat(summaries.size()).isEqualTo(5);
    }

    @Test
    public void 최고_별점_코스_조회에_성공한다_5개_미만() {
        Mockito.when(visitedCourseQueryService.queryCourseStatistics()).thenReturn(List.of(
                new CourseStat(1L, 1.4, 10L, 100L),
                new CourseStat(2L, 3.7, 20L, 21L),
                new CourseStat(5L, 8.8, 30L, 35L),
                new CourseStat(6L, 7.1, 60L, 60L)
        ));

        Mockito.when(courseRepository.findById(Mockito.anyLong())).thenReturn(
                Optional.of(new Course())
        );

        List<RecommendedCourseSummary> summaries = courseQueryService.queryTopRatedCourses(CourseQueryService.RecommendationCriteria.STARS);

        System.out.println(summaries);
        assertThat(summaries.size()).isEqualTo(4);
    }

    @Test
    public void 질병별_코스_조회에_성공한다() {
        Mockito.when(userDiseaseQueryService.queryUserDiseases(TEST_USER_ID)).thenReturn(List.of(
                UserDisease.builder().userId(TEST_USER_ID).diseaseId(1L).build(),
                UserDisease.builder().userId(TEST_USER_ID).diseaseId(2L).build(),
                UserDisease.builder().userId(TEST_USER_ID).diseaseId(3L).build())
        );
        Mockito.when(userDiseaseQueryService.queryUsersHavingDisease(Mockito.anyLong())).thenReturn(List.of(
                UserDisease.builder().userId(TEST_USER_2).diseaseId(0l).build(),
                UserDisease.builder().userId(TEST_USER_3).diseaseId(0l).build())
        );
        Mockito.when(visitedCourseQueryService.queryVisitedCourses(Mockito.anyLong())).thenReturn(List.of(
                new VisitedCourse(1L, TEST_COURSE_ID, LocalDateTime.now(), TEST_USER_ID, null, null, null, 1, 1.0, "", false, null),
                new VisitedCourse(2L, TEST_COURSE_ID + 1, LocalDateTime.now(), TEST_USER_ID, null, null, null, 2, 1.0, "", true, null),
                new VisitedCourse(3L, TEST_COURSE_ID + 2, LocalDateTime.now(), TEST_USER_ID, null, null, null, 3, 1.0, "", false, null),
                new VisitedCourse(4L, TEST_COURSE_ID + 3, LocalDateTime.now(), TEST_USER_ID, null, null, null, 4, 1.0, "", true, null),
                new VisitedCourse(5L, TEST_COURSE_ID + 4, LocalDateTime.now(), TEST_USER_ID, null, null, null, 5, 1.0, "", true, null)
        ));
        Mockito.when(courseRepository.findById(Mockito.any())).thenReturn(Optional.of(Course.builder().id(TEST_COURSE_ID).routeIdx("test001").build()));
        Mockito.when(routeQueryService.queryRouteNameByRouteIdx(Mockito.any())).thenReturn("test route name");
        Mockito.when(visitedCourseQueryService.checkVisitedCourse(Mockito.anyLong(), Mockito.anyLong())).thenReturn(Mockito.eq(true));

        List<DiseaseAndCourses> res = courseQueryService.queryCoursesForDiseasesOf(TEST_USER_ID, CourseQueryService.RecommendationCriteria.STARS, Pageable.ofSize(10));
        assertThat(res).isNotEmpty();
        Mockito.verify(userDiseaseQueryService, Mockito.times(1)).queryUserDiseases(TEST_USER_ID);
        Mockito.verify(userDiseaseQueryService, Mockito.times(3)).queryUsersHavingDisease(Mockito.anyLong());
        Mockito.verify(visitedCourseQueryService, Mockito.atLeastOnce()).queryVisitedCourses(Mockito.anyLong());
    }
}