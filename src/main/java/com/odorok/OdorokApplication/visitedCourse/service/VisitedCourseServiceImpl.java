package com.odorok.OdorokApplication.visitedCourse.service;

import com.odorok.OdorokApplication.diary.repository.VisitedCourseRepository;
import com.odorok.OdorokApplication.security.dto.CustomUserDetails;
import com.odorok.OdorokApplication.visitedCourse.dto.response.VisitedCourseSummaryWithGilName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitedCourseServiceImpl implements VisitedCourseService {

    private final VisitedCourseRepository visitedCourseRepository;

    @Override
    public List<VisitedCourseSummaryWithGilName> getVisitedCourses(CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        return visitedCourseRepository.findVisitedCoursesByUserId(userId);
    }
}
