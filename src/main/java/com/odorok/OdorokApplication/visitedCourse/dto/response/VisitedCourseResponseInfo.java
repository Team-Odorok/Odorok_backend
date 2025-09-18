package com.odorok.OdorokApplication.visitedCourse.dto.response;

import com.odorok.OdorokApplication.visitedCourse.dto.dto.CourseReview;
import com.odorok.OdorokApplication.visitedCourse.dto.dto.VisitedCourseInfo;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class VisitedCourseResponseInfo {
    private List<CourseReview> reviewList;
    private List<VisitedCourseInfo> coursesList;
}
