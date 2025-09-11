package com.odorok.OdorokApplication.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity(name = "visited_courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitedCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "visited_at", nullable = false)
    private LocalDateTime visitedAt;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "start_coords_id")
    private Long startCoordsId;

    @Column(name = "end_coords_id")
    private Long endCoordsId;

    @Column(name = "img_url", length = 500)
    private String imgUrl;

    @Column(name = "stars")
    private Integer stars;

    @Column(name = "distance")
    private Double distance;

    @Column(name = "review")
    private String review;

    @Column(name = "is_finished")
    private Boolean isFinished;

    @Column(name = "review_img_url")
    private String reviewImgUrl;
}
