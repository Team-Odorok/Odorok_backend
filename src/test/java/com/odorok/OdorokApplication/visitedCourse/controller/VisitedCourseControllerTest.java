package com.odorok.OdorokApplication.visitedCourse.controller;

import com.odorok.OdorokApplication.commons.exception.NotFoundException;
import com.odorok.OdorokApplication.diary.dto.gpt.VisitedAdditionalAttraction;
import com.odorok.OdorokApplication.domain.User;
import com.odorok.OdorokApplication.security.dto.CustomUserDetails;
import com.odorok.OdorokApplication.security.filter.KakaoLoginFilter;
import com.odorok.OdorokApplication.security.service.UserQueryService;
import com.odorok.OdorokApplication.visitedCourse.dto.response.VisitedCourseDetail;
import com.odorok.OdorokApplication.visitedCourse.dto.response.VisitedCourseSummaryWithGilName;
import com.odorok.OdorokApplication.visitedCourse.service.VisitedCourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@WebMvcTest(VisitedCourseController.class)
@AutoConfigureMockMvc(addFilters = false)
public class VisitedCourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VisitedCourseService visitedCourseService;

    @MockitoBean
    private KakaoLoginFilter kakaoLoginFilter;


    private CustomUserDetails testUserDetails;
    private final long testUserId = 1L;

    @BeforeEach
    void setUp() {
        User user = User.builder().id(testUserId).email("test@example.com").role("ROLE_USER").build();
        testUserDetails = new CustomUserDetails(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(testUserDetails, null, List.of(new SimpleGrantedAuthority(user.getRole())));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

//    @Test
//    @DisplayName("방문 완료 코스 목록 조회 성공")
//    void getVisitedCourses_Success() throws Exception {
//        // given
//        LocalDateTime visitedAt = LocalDateTime.of(2025, 9, 11, 10, 30);
//        List<VisitedCourseSummaryWithGilName> mockResponse = Collections.singletonList(
//                new VisitedCourseSummaryWithGilName(1L, visitedAt, "해파랑길", "해파랑길 1코스")
//        );
//
//        given(visitedCourseService.getVisitedCourses(testUserId)).willReturn(mockResponse);
//
//        // when
//        ResultActions resultActions = mockMvc.perform(get("/api/visited-courses")
//                .contentType(MediaType.APPLICATION_JSON));
//
//        // then
//        resultActions.andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.data[0].id").value(1L))
//                .andExpect(jsonPath("$.data[0].visitedAt").value("2025-09-11T10:30:00"))
//                .andExpect(jsonPath("$.data[0].gilName").value("해파랑길"))
//                .andExpect(jsonPath("$.data[0].courseName").value("해파랑길 1코스"))
//                .andDo(print());
//    }

    @Test
    @DisplayName("방문 완료 코스 상세 정보 조회 성공")
    void getVisitedCourseDetail_Success() throws Exception {
        // given
        long visitedCourseId = 1L;
        VisitedCourseDetail mockResponse = new VisitedCourseDetail(visitedCourseId, LocalDateTime.now(), 101L, "해파랑길", "해파랑길 1코스", 10.5, 5, "리뷰 내용", "image.url");
        mockResponse.setAverageStars(4.5);
        mockResponse.setVisitedAttractions(Collections.singletonList(new VisitedAdditionalAttraction("호미곶", "주소", "설명")));

        given(visitedCourseService.getVisitedCourseDetail(testUserId, visitedCourseId)).willReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/visited-courses/{id}", visitedCourseId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(visitedCourseId))
                .andExpect(jsonPath("$.data.courseName").value("해파랑길 1코스"))
                .andExpect(jsonPath("$.data.averageStars").value(4.5))
                .andExpect(jsonPath("$.data.visitedAttractions[0].title").value("호미곶"))
                .andDo(print());
    }

//    @Test
//    @DisplayName("존재하지 않는 방문 코스 상세 정보 조회 시 404 반환")
//    void getVisitedCourseDetail_NotFound() throws Exception {
//        // given
//        long nonExistentId = 999L;
//        given(visitedCourseService.getVisitedCourseDetail(testUserId, nonExistentId))
//                .willThrow(new NotFoundException("해당 방문 코스 정보를 찾을 수 없거나 소유자가 아닙니다."));
//
//        // when
//        ResultActions resultActions = mockMvc.perform(get("/api/visited-courses/{id}", nonExistentId)
//                .contentType(MediaType.APPLICATION_JSON));
//
//        // then
//        resultActions.andExpect(status().isNotFound())
//                .andDo(print());
//    }

    @Test
    void createOrUpdateReview_Success() throws Exception {
        long visitedCourseId = 1L;
    
        // file only for the image
        MockMultipartFile image =
            new MockMultipartFile("image", "test.jpg",
                MediaType.IMAGE_JPEG_VALUE, "test image".getBytes());
    
        doNothing().when(visitedCourseService)
            .createOrUpdateReview(anyLong(), anyLong(), anyInt(), anyString(), any());
    
        ResultActions resultActions = mockMvc
            .perform(MockMvcRequestBuilders.multipart("/api/visited-courses/{id}/reviews", visitedCourseId)
                .file(image)
                // send simple form fields (multipart/form-data) instead of files
                .param("star", "5")
                .param("review", "좋은 후기")
                .with(request -> { request.setMethod("POST"); return request; })
                .with(authentication(SecurityContextHolder.getContext().getAuthentication())));
    
        resultActions.andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.message").value("후기 작성 성공"));
    }
}
