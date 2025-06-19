package com.odorok.OdorokApplication.infrastructures.repository;

import com.odorok.OdorokApplication.infrastructures.domain.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
}
