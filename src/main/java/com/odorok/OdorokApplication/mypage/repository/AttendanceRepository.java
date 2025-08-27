package com.odorok.OdorokApplication.mypage.repository;

import com.odorok.OdorokApplication.domain.AttendanceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceHistory, Long>, AttendanceRepositoryCustom {
}
