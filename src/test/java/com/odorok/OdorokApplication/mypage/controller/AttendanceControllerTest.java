package com.odorok.OdorokApplication.mypage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odorok.OdorokApplication.domain.AttendanceHistory;
import com.odorok.OdorokApplication.domain.User;
import com.odorok.OdorokApplication.mypage.dto.response.AttendanceResponseDto;
import com.odorok.OdorokApplication.mypage.repository.AttendanceRepository;
import com.odorok.OdorokApplication.mypage.service.AttendanceService;
import com.odorok.OdorokApplication.security.dto.CustomUserDetails;
import com.odorok.OdorokApplication.security.repository.AuthUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttendanceApiController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AttendanceRepository attendanceRepository;

    @MockitoBean
    private AttendanceService attendanceService; // Service도 Mock 처리

    @MockitoBean
    private AuthUserRepository authUserRepository;

    private User testUser;
    private CustomUserDetails customUserDetails;

    private final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        // User 객체 생성
        testUser = User.builder()
                .email("jihun@example.com")
                .id(TEST_USER_ID)
                .role("USER")
                .build();

        // CustomUserDetails 객체 생성
        customUserDetails = new CustomUserDetails(testUser);

        // Security Context 설정
        Authentication auth = new UsernamePasswordAuthenticationToken(
                customUserDetails,
                null,
                List.of(new SimpleGrantedAuthority("USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("오늘 날짜로 출석 체크 요청 시 성공한다")
    void registTodayAttendance() throws Exception {
        // given: Mock 동작 정의
        AttendanceHistory savedAttendance = AttendanceHistory.builder()
                .id(1L)
                .userId(TEST_USER_ID)
                .attendedAt(LocalDateTime.now())
                .build();

        // when & then
        mockMvc.perform(post("/api/attendances")
                        .with(user(customUserDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("출석체크 요청 성공"));
    }

    @Test
    @DisplayName("특정 월의 출석 기록 조회 시, 해당 월의 출석 날짜 목록을 반환한다")
    void searchAttendanceByMonth() throws Exception {
        // given: Mock 동작 정의
        List<Integer> expectedDays = List.of(5, 6, 10, 25);
        AttendanceResponseDto response = new AttendanceResponseDto(expectedDays, 2025, 8);

        // Service의 월별 출석 조회 메서드 Mock 설정
        when(attendanceService.findAttendanceInfoByMonth(eq(TEST_USER_ID), eq(2025), eq(8)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/attendances")
                        .param("year", "2025")
                        .param("month", "8")
                        .with(user(customUserDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용자 출석 일자 요청 성공"))
                .andExpect(jsonPath("$.data.days", hasSize(4)))
                .andExpect(jsonPath("$.data.days", contains(5, 6, 10, 25)));
    }
}