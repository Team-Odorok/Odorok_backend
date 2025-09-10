package com.odorok.OdorokApplication.visitedCourse.service;

import com.odorok.OdorokApplication.visitedCourse.dto.response.VisitedCourseDetail;
import com.odorok.OdorokApplication.visitedCourse.dto.response.VisitedCourseSummaryWithGilName;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VisitedCourseService {
    List<VisitedCourseSummaryWithGilName> getVisitedCourses(Long userId);
    VisitedCourseDetail getVisitedCourseDetail(Long userId, Long visitedCourseId);
    void createOrUpdateReview(Long userId, Long visitedCourseId, int star, String review, MultipartFile image);
}
