package com.odorok.OdorokApplication.course.repository;

import com.odorok.OdorokApplication.infrastructures.domain.PathCoord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PathCoordRepository extends JpaRepository<PathCoord, Long> {
    List<PathCoord> findByCourseId(Long courseId);

}
