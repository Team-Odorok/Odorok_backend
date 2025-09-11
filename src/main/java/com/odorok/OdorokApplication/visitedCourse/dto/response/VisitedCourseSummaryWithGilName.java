package com.odorok.OdorokApplication.visitedCourse.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class VisitedCourseSummaryWithGilName {
    private Long id;
    private LocalDateTime visitedAt;
    private String gilName;
    private String courseName;

    @QueryProjection
    public VisitedCourseSummaryWithGilName(Long id, LocalDateTime visitedAt, String gilName, String courseName) {
        this.id = id;
        this.visitedAt = visitedAt;
        this.gilName = gilName;
        this.courseName = courseName;
    }
}
