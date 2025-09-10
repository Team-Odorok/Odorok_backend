package com.odorok.OdorokApplication.visitedCourse.service;

import com.odorok.OdorokApplication.commons.exception.NotFoundException;
import com.odorok.OdorokApplication.diary.dto.gpt.VisitedAdditionalAttraction;
import com.odorok.OdorokApplication.diary.repository.VisitedCourseRepository;
import com.odorok.OdorokApplication.domain.VisitedCourse;
import com.odorok.OdorokApplication.s3.service.S3Service;
import com.odorok.OdorokApplication.visitedCourse.dto.response.VisitedCourseDetail;
import com.odorok.OdorokApplication.visitedCourse.dto.response.VisitedCourseSummaryWithGilName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VisitedCourseServiceTest {

    @InjectMocks
    private VisitedCourseServiceImpl visitedCourseService;

    @Mock
    private VisitedCourseRepository visitedCourseRepository;

    @Mock
    private S3Service s3Service;

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

    @Test
    @DisplayName("방문 완료 코스 상세 정보 조회 성공")
    void getVisitedCourseDetail_Success() {
        // given
        long userId = 1L;
        long visitedCourseId = 1L;
        long courseId = 101L;

        VisitedCourseDetail mockDetail = new VisitedCourseDetail(visitedCourseId, LocalDateTime.now(), courseId, "해파랑길", "해파랑길 1코스", 10.5, 5, "리뷰 내용", "image.url");
        List<VisitedAdditionalAttraction> mockAttractions = Collections.singletonList(new VisitedAdditionalAttraction("호미곶", "주소", "설명"));
        Double mockAvgStars = 4.5;

        given(visitedCourseRepository.findDetailById(userId, visitedCourseId)).willReturn(Optional.of(mockDetail));
        given(visitedCourseRepository.findVisitedAttractionByVisitedCourseId(userId, visitedCourseId)).willReturn(mockAttractions);
        given(visitedCourseRepository.findAvgStarsByCourseId(courseId)).willReturn(mockAvgStars);

        // when
        VisitedCourseDetail result = visitedCourseService.getVisitedCourseDetail(userId, visitedCourseId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCourseName()).isEqualTo("해파랑길 1코스");
        assertThat(result.getAverageStars()).isEqualTo(mockAvgStars);
        assertThat(result.getVisitedAttractions()).hasSize(1);
        assertThat(result.getVisitedAttractions().get(0).getTitle()).isEqualTo("호미곶");

        verify(visitedCourseRepository, times(1)).findDetailById(userId, visitedCourseId);
        verify(visitedCourseRepository, times(1)).findVisitedAttractionByVisitedCourseId(userId, visitedCourseId);
        verify(visitedCourseRepository, times(1)).findAvgStarsByCourseId(courseId);
    }

    @Test
    @DisplayName("존재하지 않는 방문 코스 상세 정보 조회 시 예외 발생")
    void getVisitedCourseDetail_NotFound() {
        // given
        long userId = 1L;
        long nonExistentId = 999L;
        given(visitedCourseRepository.findDetailById(userId, nonExistentId)).willReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> {
            visitedCourseService.getVisitedCourseDetail(userId, nonExistentId);
        });

        verify(visitedCourseRepository, times(1)).findDetailById(userId, nonExistentId);
    }

    @Test
    @DisplayName("후기 작성/수정 성공")
    void createOrUpdateReview_Success() {
        // given
        long userId = 1L;
        long visitedCourseId = 1L;
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test image".getBytes());
        VisitedCourse mockVisitedCourse = VisitedCourse.builder().id(visitedCourseId).userId(userId).build();
        ArgumentCaptor<VisitedCourse> captor = ArgumentCaptor.forClass(VisitedCourse.class);

        given(visitedCourseRepository.findById(visitedCourseId)).willReturn(Optional.of(mockVisitedCourse));
        given(s3Service.uploadMany(anyString(), anyString(), anyList())).willReturn(List.of("http://new.image.url"));

        // when
        visitedCourseService.createOrUpdateReview(userId, visitedCourseId, 5, "새로운 후기", image);

        // then
        verify(visitedCourseRepository, times(1)).save(captor.capture());
        VisitedCourse savedCourse = captor.getValue();
        assertThat(savedCourse.getStars()).isEqualTo(5);
        assertThat(savedCourse.getReview()).isEqualTo("새로운 후기");
        assertThat(savedCourse.getImgUrl()).isEqualTo("http://new.image.url");
    }

    @Test
    @DisplayName("후기 작성 시 소유자가 아니면 예외 발생")
    void createOrUpdateReview_AccessDenied() {
        // given
        long userId = 1L;
        long otherUserId = 2L;
        long visitedCourseId = 1L;
        VisitedCourse mockVisitedCourse = VisitedCourse.builder().id(visitedCourseId).userId(otherUserId).build();

        given(visitedCourseRepository.findById(visitedCourseId)).willReturn(Optional.of(mockVisitedCourse));

        // when & then
        assertThrows(AccessDeniedException.class, () -> {
            visitedCourseService.createOrUpdateReview(userId, visitedCourseId, 5, "후기", null);
        });
    }
}
