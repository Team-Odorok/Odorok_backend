package com.odorok.OdorokApplication.community.repository;

import com.odorok.OdorokApplication.draftDomain.Disease;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiseaseRepository extends JpaRepository<Disease,Long> {
}
