package com.odorok.OdorokApplication.visitedCourse.dto.response;

import com.odorok.OdorokApplication.diary.dto.gpt.VisitedAdditionalAttraction;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class VisitedCourseDetail {
    private Long id;
    private LocalDateTime visitedAt;
    private Long courseId;
    private String gilName;
    private String courseName;
    private Double distance;
    private Integer stars;
    private String review;
    private Double averageStars;
    private String reviewImgUrl;
    private List<VisitedAdditionalAttraction> visitedAttractions;

    @QueryProjection
    public VisitedCourseDetail(Long id, LocalDateTime visitedAt, Long courseId, String gilName, String courseName, Double distance, Integer stars, String review, String reviewImgUrl) {
        this.id = id;
        this.visitedAt = visitedAt;
        this.courseId = courseId;
        this.gilName = gilName;
        this.courseName = courseName;
        this.distance = distance;
        this.stars = stars;
        this.review = review;
        this.reviewImgUrl = reviewImgUrl;
    }
}
