package com.odorok.OdorokApplication.diary.repository;

import com.odorok.OdorokApplication.course.dto.process.CourseStat;
import com.odorok.OdorokApplication.domain.VisitedCourse;
import com.odorok.OdorokApplication.visitedCourse.dto.dto.VisitedCourseView;
import com.odorok.OdorokApplication.visitedCourse.dto.dto.VisitedCourseInfo;
import com.odorok.OdorokApplication.visitedCourse.dto.dto.VisitedCourseView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VisitedCourseRepository extends JpaRepository<VisitedCourse, Long>, VisitedCourseRepositoryCustom  {
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
    List<VisitedCourse> findByUserId(Long userId);

    @Query(value = "SELECT AVG(v.stars) FROM visited_courses v WHERE v.course_id = :courseId", nativeQuery = true)
    Double findAvgStarsByCourseId(@Param("courseId") Long courseId);

    @Query(value = "SELECT COUNT(*) FROM visited_courses v WHERE v.course_id = :courseId and v.review IS NOT NULL", nativeQuery = true)
    Long countReviewsOf(@Param("courseId") Long courseId);
    @Query("""
            select v.distance
            from visited_courses v
            where v.userId = :userId
            """)
    List<Integer> findAllDistanceByUserId(@Param("userId")Long id);

    Optional<VisitedCourse> findByUserIdAndCourseId(Long userId, Long courseId);
    @Query(value = """
        SELECT
          v.visited_at AS visitedAt,
          v.course_id  AS courseId,
          v.distance   AS distance,
          v.stars      AS stars,
          v.review     AS review,
          c.name       AS courseName
        FROM visited_courses v
        JOIN courses c ON c.id = v.course_id
        WHERE v.user_id = :userId
        """, nativeQuery = true)
    List<VisitedCourseView> findVisitedCoursesAndReviewByUserId(@Param("userId") Long userId);

    Optional<VisitedCourse> findByCourseIdAndUserId(Long visitedCourseId, Long userId);
}
