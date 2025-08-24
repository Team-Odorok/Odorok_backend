package com.odorok.OdorokApplication.mypage.repository;

import com.odorok.OdorokApplication.draftDomain.Tier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TierRepository extends JpaRepository<Tier,Long> {
}
