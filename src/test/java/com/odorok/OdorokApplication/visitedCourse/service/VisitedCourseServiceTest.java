package com.odorok.OdorokApplication.visitedCourse.service;

import com.odorok.OdorokApplication.diary.repository.VisitedCourseRepository;
import com.odorok.OdorokApplication.visitedCourse.dto.response.VisitedCourseSummaryWithGilName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VisitedCourseServiceTest {

    @InjectMocks
    private VisitedCourseServiceImpl visitedCourseService;

    @Mock
    private VisitedCourseRepository visitedCourseRepository;

    @Test
    @DisplayName("사용자 ID로 방문 완료 코스 목록 조회 성공")
    void getVisitedCourses_Success() {
        // given
        long userId = 1L;
        LocalDateTime visitedAt = LocalDateTime.of(2025, 9, 11, 10, 30);
        List<VisitedCourseSummaryWithGilName> mockResponse = Collections.singletonList(
                new VisitedCourseSummaryWithGilName(1L, visitedAt, "해파랑길", "해파랑길 1코스")
        );

        given(visitedCourseRepository.findVisitedCoursesByUserId(userId)).willReturn(mockResponse);

        // when
        List<VisitedCourseSummaryWithGilName> result = visitedCourseService.getVisitedCourses(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getCourseName()).isEqualTo("해파랑길 1코스");

        verify(visitedCourseRepository, times(1)).findVisitedCoursesByUserId(userId);
    }
}
