package com.odorok.OdorokApplication.visitedCourse.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class CourseReview {
    private String courseName;
    private Integer stars;
    private String review;
    private Long courseId;
}
