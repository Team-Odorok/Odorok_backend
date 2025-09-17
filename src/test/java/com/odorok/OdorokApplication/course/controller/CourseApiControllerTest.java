package com.odorok.OdorokApplication.course.controller;

import com.odorok.OdorokApplication.course.dto.response.item.*;
import com.odorok.OdorokApplication.course.service.CourseQueryService;
import com.odorok.OdorokApplication.course.service.CourseScheduleManageService;
import com.odorok.OdorokApplication.course.service.CourseScheduleQueryService;
import com.odorok.OdorokApplication.course.service.ProfileQueryService;
import com.odorok.OdorokApplication.domain.User;
import com.odorok.OdorokApplication.draftDomain.Profile;
import com.odorok.OdorokApplication.infrastructures.domain.Course;
import com.odorok.OdorokApplication.security.dto.CustomUserDetails;
import com.odorok.OdorokApplication.security.filter.KakaoLoginFilter;
import com.odorok.OdorokApplication.security.jwt.JWTUtil;
import com.odorok.OdorokApplication.security.service.AuthService;
import com.odorok.OdorokApplication.security.service.UserQueryService;
import com.odorok.OdorokApplication.security.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { CourseApiController.class })
@AutoConfigureMockMvc(addFilters = false)
class CourseApiControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourseQueryService courseQueryService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private ProfileQueryService profileQueryService;
    @MockitoBean
    private CourseScheduleManageService courseScheduleManageService;
    @MockitoBean
    private CourseScheduleQueryService courseScheduleQueryService;

    @MockitoBean
    private KakaoLoginFilter kakaoLoginFilter;

    private final static Long TEST_USER_ID = 1L;
    private final static String TEST_USER_EMAIL = "email";
    private final static Long TEST_COURSE_ID = 1L;
    private final static Integer TEST_SIDO_CODE = 6;
    private final static Integer TEST_SIGUNGU_CODE = 10;


    @BeforeEach
    public void setup() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(
                        User.builder().email(TEST_USER_EMAIL).id(TEST_USER_ID).role("USER").build()),
                        null,
                        List.of(new SimpleGrantedAuthority("USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void 가입된_사용자가_지역_코드로_코스_조회에_성공한다() throws Exception {
        System.out.println("프린서펄 = " + SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        // given
        Mockito.when(courseQueryService.checkSidoCodeValidation(TEST_SIDO_CODE)).thenReturn(true);
        Mockito.when(userService.queryByEmail(TEST_USER_EMAIL)).thenReturn(User.builder().id(TEST_USER_ID).build());
        Mockito.when(courseQueryService.queryCoursesByRegion(Mockito.any(), Mockito.eq(TEST_SIGUNGU_CODE), Mockito.eq(1L), Mockito.any())).thenReturn(List.of(
                CourseSummary.builder()
                        .courseId(1l).courseName("코스1").gilName("GIL001")
                        .createdAt(LocalDateTime.now().toLocalDate()).modifiedAt(LocalDateTime.now().toLocalDate()).build(),
                CourseSummary.builder()
                        .courseId(2l).courseName("코스2").gilName("GIL002")
                        .createdAt(LocalDateTime.now().toLocalDate()).modifiedAt(LocalDateTime.now().toLocalDate()).build()
        ));

        // when
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/courses/region")
                        .param("sidoCode", TEST_SIDO_CODE.toString())
                        .param("sigunguCode", TEST_SIGUNGU_CODE.toString())
                        .param("page", "0").param("size", "10").accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.items[0].courseId").value(1l));
    }

    @Test
    public void 가입된_사용자가_지역_코드로_코스_조회에_실패한다() throws Exception {
        // given
        Mockito.when(userService.queryByEmail(TEST_USER_EMAIL)).thenReturn(User.builder().id(TEST_USER_ID).build());
        Mockito.when(courseQueryService.queryCoursesByRegion(Mockito.eq(TEST_SIDO_CODE), Mockito.eq(TEST_SIGUNGU_CODE), Mockito.eq(1L), Mockito.any())).thenReturn(List.of(
                CourseSummary.builder()
                        .courseId(1L).courseName("코스1").gilName("GIL001")
                        .createdAt(LocalDateTime.now().toLocalDate()).modifiedAt(LocalDateTime.now().toLocalDate()).build(),
                CourseSummary.builder()
                        .courseId(2l).courseName("코스2").gilName("GIL002")
                        .createdAt(LocalDateTime.now().toLocalDate()).modifiedAt(LocalDateTime.now().toLocalDate()).build()
        ));
        Mockito.when(courseQueryService.checkSidoCodeValidation(TEST_SIDO_CODE)).thenReturn(false);
        // when
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/courses/region")
                .param("sidoCode", TEST_SIDO_CODE.toString())
                .param("sigunguCode", TEST_SIGUNGU_CODE.toString())
                .param("page", "0").param("size", "10").accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void 전체코스_조회에_성공한다() throws Exception{
        Mockito.when(userService.queryByEmail(TEST_USER_EMAIL)).thenReturn(User.builder().id(TEST_USER_ID).build());
        Mockito.when(courseQueryService.queryAllCourses(Mockito.eq(TEST_USER_ID), Mockito.any(Pageable.class))).thenReturn(List.of(
                        CourseSummary.builder()
                                .courseId(1l).courseName("코스1").gilName("GIL001")
                                .createdAt(LocalDateTime.now().toLocalDate()).modifiedAt(LocalDateTime.now().toLocalDate()).build(),
                        CourseSummary.builder()
                                .courseId(2l).courseName("코스2").gilName("GIL002")
                                .createdAt(LocalDateTime.now().toLocalDate()).modifiedAt(LocalDateTime.now().toLocalDate()).build()
                ));


        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/api/courses")
                .param("size", "10").param("page", "0"));

        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.items[0].courseId").value(1L));
    }

    @Test
    public void 코스_상세_조회에_성공한다() throws Exception {
        Mockito.when(courseQueryService.queryCourseDetail(TEST_COURSE_ID))
                .thenReturn(new CourseDetail("요약", "전체", "여행", 6, 1000L, List.of(new Coord())));

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/api/courses/detail")
                .param("courseId", "1"));

        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.summary").value("요약"));
        resultActions.andExpect(jsonPath("$.data.contents").value("전체"));
        resultActions.andExpect(jsonPath("$.data.avgStars").value(6));
        resultActions.andExpect(jsonPath("$.data.reviewCount").value(1000));
        resultActions.andExpect(jsonPath("$.data.coords").isNotEmpty());
    }

    @Test
    public void 별점_상위_코스_조회에_성공한다() throws Exception {
        Mockito.when(courseQueryService.queryTopRatedCourses(Mockito.any(CourseQueryService.RecommendationCriteria.class)))
                .thenReturn(List.of(
                        new RecommendedCourseSummary(new Course(), 6, 10, 15L),
                        new RecommendedCourseSummary(new Course(), 7, 15, 20L),
                        new RecommendedCourseSummary(new Course(), 8, 15, 20L),
                        new RecommendedCourseSummary(new Course(), 9, 16, 20L),
                        new RecommendedCourseSummary(new Course(), 10, 20, 20L)
                ));

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/api/courses/top"));

        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.topStars").isNotEmpty());
        resultActions.andExpect(jsonPath("$.data.topVisited").isNotEmpty());
        resultActions.andExpect(jsonPath("$.data.topReviewCount").isNotEmpty());
        resultActions.andDo(print());

        Mockito.verify(courseQueryService, Mockito.times(3)).queryTopRatedCourses(Mockito.any(CourseQueryService.RecommendationCriteria.class));
    }

    @Test
    public void 질병_코스_조회에_성공한다() throws Exception {
        Mockito.when(userService.queryByEmail("email")).thenReturn(User.builder().id(TEST_USER_ID).build());
        Mockito.when(courseQueryService.queryCoursesForDiseasesOf(Mockito.eq(TEST_USER_ID), Mockito.any(), Mockito.any(Pageable.class)))
                .thenReturn(List.of(
                        new DiseaseAndCourses(1L, List.of()),
                        new DiseaseAndCourses(3L, List.of()),
                        new DiseaseAndCourses(5L, List.of())
                ));

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/api/courses/disease"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.items").isNotEmpty());
        resultActions.andDo(print());

        Mockito.verify(courseQueryService, Mockito.times(1)).queryCoursesForDiseasesOf(Mockito.eq(TEST_USER_ID), Mockito.any(), Mockito.any(Pageable.class));
    }

    @Test
    public void 사용자_지역의_코스를_조회하는데_성공한다() throws Exception {
        Mockito.when(userService.queryByEmail("email")).thenReturn(User.builder().id(TEST_USER_ID).build());
        Mockito.when(courseQueryService.checkSidoCodeValidation(TEST_SIDO_CODE)).thenReturn(true);
        Mockito.when(profileQueryService.queryProfileByUserId(Mockito.eq(TEST_USER_ID)))
                .thenReturn(Profile.builder().sidoCode(TEST_SIDO_CODE).sigunguCode(TEST_SIGUNGU_CODE).userId(TEST_USER_ID).build());
        Mockito.when(courseQueryService.queryCoursesByRegion(Mockito.eq(TEST_SIDO_CODE), Mockito.eq(TEST_SIGUNGU_CODE), Mockito.eq(TEST_USER_ID), Mockito.any(Pageable.class)))
                .thenReturn(List.of(
                        CourseSummary.builder()
                                .courseId(1l).courseName("코스1").gilName("GIL001")
                                .createdAt(LocalDateTime.now().toLocalDate()).modifiedAt(LocalDateTime.now().toLocalDate()).build(),
                        CourseSummary.builder()
                                .courseId(2l).courseName("코스2").gilName("GIL002")
                                .createdAt(LocalDateTime.now().toLocalDate()).modifiedAt(LocalDateTime.now().toLocalDate()).build()
                ));

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/api/courses/user-region"));

        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.items").isNotEmpty());

        Mockito.verify(profileQueryService, Mockito.atLeastOnce()).queryProfileByUserId(Mockito.eq(TEST_USER_ID));
    }


    @Test
    public void 사용자의_방문예정_조회에_성공한다() throws Exception {
        Mockito.when(userService.queryByEmail("email")).thenReturn(User.builder().email("email").id(TEST_USER_ID).build());
        Mockito.when(courseScheduleQueryService.queryAllSchedule(TEST_USER_ID)).thenReturn(List.of(
                VisitationScheduleSummary.builder().courseName("TEST1").build(),
                VisitationScheduleSummary.builder().courseName("TEST2").build()
        ));

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/api/courses/schedule"));

        resultActions.andExpect(status().isOk());
        resultActions.andDo(print());
        resultActions.andExpect(jsonPath("$.data.schedule").isNotEmpty());
    }
}