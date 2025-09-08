package com.odorok.OdorokApplication.course.repository;

import com.odorok.OdorokApplication.domain.UserDisease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDiseaseRepository extends JpaRepository<UserDisease, Long> {
    List<UserDisease> findByUserId(Long userId);
    List<UserDisease> findByDiseaseId(Long diseaseId);
    @Query("""
            select ud.diseaseId
            from user_diseases ud
            where userId = :userId
            """)
    List<Long> findAllDiseaseIdByUserId(@Param("userId")Long id);

    void deleteAllByUserId(Long id);
}
