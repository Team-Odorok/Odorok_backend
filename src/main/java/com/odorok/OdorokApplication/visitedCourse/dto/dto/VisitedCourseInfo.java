package com.odorok.OdorokApplication.visitedCourse.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Builder
public class VisitedCourseInfo {
    private LocalDateTime visitedAt;
    private Long courseId;
    private BigDecimal distance;
    private String courseName;
}
