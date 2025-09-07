package com.odorok.OdorokApplication.mypage.repository;

import com.odorok.OdorokApplication.domain.HealthInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthInfoRepository extends JpaRepository<HealthInfo,Long> {
    HealthInfo findByUserId(Long id);
}
