package com.odorok.OdorokApplication.course.repository;

import com.odorok.OdorokApplication.infrastructures.domain.PathCoord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PathCoordRepository extends JpaRepository<PathCoord, Long> {
    List<PathCoord> findByCourseId(Long courseId);
    @Query(value = """
            SELECT id
            FROM path_coords
            WHERE course_id = :courseId
            ORDER BY ordering ASC
            LIMIT 1
            """, nativeQuery = true)
    Long findOneByCourseId(@Param("courseId")Long courseId);
    @Query(value = """
            SELECT id
            FROM path_coords
            WHERE course_id = :courseId
            ORDER BY ordering DESC
            LIMIT 1
            """, nativeQuery = true)
    Long findLastByCourseId(@Param("courseId")Long courseId);
}
