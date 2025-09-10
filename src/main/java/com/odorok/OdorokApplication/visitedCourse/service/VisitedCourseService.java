package com.odorok.OdorokApplication.visitedCourse.service;

import com.odorok.OdorokApplication.visitedCourse.dto.response.VisitedCourseDetail;
import com.odorok.OdorokApplication.visitedCourse.dto.response.VisitedCourseSummaryWithGilName;

import java.util.List;

public interface VisitedCourseService {
    List<VisitedCourseSummaryWithGilName> getVisitedCourses(Long userId);
    VisitedCourseDetail getVisitedCourseDetail(Long userId, Long visitedCourseId);
}
