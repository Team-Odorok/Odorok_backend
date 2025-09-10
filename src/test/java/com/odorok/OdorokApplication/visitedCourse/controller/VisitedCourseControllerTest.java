package com.odorok.OdorokApplication.visitedCourse.controller;

import com.odorok.OdorokApplication.domain.User;
import com.odorok.OdorokApplication.security.dto.CustomUserDetails;
import com.odorok.OdorokApplication.visitedCourse.dto.response.VisitedCourseSummaryWithGilName;
import com.odorok.OdorokApplication.visitedCourse.service.VisitedCourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VisitedCourseController.class)
public class VisitedCourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VisitedCourseService visitedCourseService;

    private CustomUserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        User user = User.builder().id(1L).email("test@example.com").role("ROLE_USER").build();
        testUserDetails = new CustomUserDetails(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(testUserDetails, null, List.of(new SimpleGrantedAuthority(user.getRole())));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("방문 완료 코스 목록 조회 성공")
    void getVisitedCourses_Success() throws Exception {
        // given
        LocalDateTime visitedAt = LocalDateTime.of(2025, 9, 11, 10, 30);
        List<VisitedCourseSummaryWithGilName> mockResponse = Collections.singletonList(
                new VisitedCourseSummaryWithGilName(1L, visitedAt, "해파랑길", "해파랑길 1코스")
        );

        given(visitedCourseService.getVisitedCourses(1L)).willReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/visited-courses")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].visitedAt").value("2025-09-11T10:30:00"))
                .andExpect(jsonPath("$.data[0].gilName").value("해파랑길"))
                .andExpect(jsonPath("$.data[0].courseName").value("해파랑길 1코스"))
                .andDo(print());
    }
}
