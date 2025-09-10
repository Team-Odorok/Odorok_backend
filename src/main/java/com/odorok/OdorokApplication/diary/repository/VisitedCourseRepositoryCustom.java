package com.odorok.OdorokApplication.diary.repository;

import com.odorok.OdorokApplication.course.dto.process.CourseStat;
import com.odorok.OdorokApplication.diary.dto.gpt.VisitedAdditionalAttraction;
import com.odorok.OdorokApplication.diary.dto.gpt.VisitedCourseAndAttraction;
import com.odorok.OdorokApplication.diary.dto.response.VisitedCourseSummary;
import com.odorok.OdorokApplication.visitedCourse.dto.response.VisitedCourseDetail;
import com.odorok.OdorokApplication.visitedCourse.dto.response.VisitedCourseSummaryWithGilName;

import java.util.List;
import java.util.Optional;

public interface VisitedCourseRepositoryCustom {
    List<VisitedAdditionalAttraction> findVisitedAttractionByVisitedCourseId(Long userId, Long visitedCourseId);
    VisitedCourseAndAttraction findCourseAndAttractionsByVisitedCourseId(Long userId, Long visitedCourseId);
    List<VisitedCourseSummary> findVisitedCourseWithoutDiaryByUserId(Long userId);
    List<CourseStat> summarizeCourseFeedback();
    List<VisitedCourseSummaryWithGilName> findVisitedCoursesByUserId(Long userId);
    Optional<VisitedCourseDetail> findDetailById(Long userId, Long visitedCourseId);
}
