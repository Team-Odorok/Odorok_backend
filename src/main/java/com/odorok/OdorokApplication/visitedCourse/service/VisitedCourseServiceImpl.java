package com.odorok.OdorokApplication.visitedCourse.service;

import com.odorok.OdorokApplication.commons.exception.NotFoundException;
import com.odorok.OdorokApplication.diary.dto.gpt.VisitedAdditionalAttraction;
import com.odorok.OdorokApplication.diary.repository.VisitedCourseRepository;
import com.odorok.OdorokApplication.visitedCourse.dto.response.VisitedCourseDetail;
import com.odorok.OdorokApplication.visitedCourse.dto.response.VisitedCourseSummaryWithGilName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitedCourseServiceImpl implements VisitedCourseService {

    private final VisitedCourseRepository visitedCourseRepository;

    @Override
    public List<VisitedCourseSummaryWithGilName> getVisitedCourses(Long userId) {
        return visitedCourseRepository.findVisitedCoursesByUserId(userId);
    }

    @Override
    public VisitedCourseDetail getVisitedCourseDetail(Long userId, Long visitedCourseId) {
        // 방문 코스 조회
        VisitedCourseDetail detail = visitedCourseRepository.findDetailById(userId, visitedCourseId)
                .orElseThrow(() -> new NotFoundException("해당 방문 코스 정보를 찾을 수 없거나 소유자가 아닙니다."));

        // 방문한 다른 명소 조회
        List<VisitedAdditionalAttraction> attractions = visitedCourseRepository.findVisitedAttractionByVisitedCourseId(userId, visitedCourseId);
        detail.setVisitedAttractions(attractions);

        // 코스에 대한 평균 별점 조회
        Double avgStars = visitedCourseRepository.findAvgStarsByCourseId(detail.getCourseId());
        detail.setAverageStars(avgStars);

        return detail;
    }
}
