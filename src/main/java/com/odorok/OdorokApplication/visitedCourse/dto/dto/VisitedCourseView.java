package com.odorok.OdorokApplication.visitedCourse.dto.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface VisitedCourseView {
    LocalDateTime getVisitedAt();
    Long getCourseId();
    BigDecimal getDistance();   // DECIMAL이면 BigDecimal 권장
    Integer getStars();
    String getReview();
    String getCourseName();
}
