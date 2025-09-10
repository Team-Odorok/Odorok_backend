package com.odorok.OdorokApplication.diary.repository;

import com.odorok.OdorokApplication.draftDomain.PurchaseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseHistoryRepository extends JpaRepository<PurchaseHistory, Long> {
}
