package com.odorok.OdorokApplication.attraction.repository;

import com.odorok.OdorokApplication.draftDomain.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.w3c.dom.Attr;

import java.util.List;

@Repository
public interface AttractionRepository extends JpaRepository<Attraction, Long> {
    List<Attraction> findBySidoCodeAndSigunguCodeAndContentTypeId(Integer sidoCode, Integer sigunguCode, Integer contentTypeId);
    List<Attraction> findByLatitudeBetweenAndLongitudeBetweenAndContentTypeId(double latMin, double latMax, double lonMin, double lonMax, int contentTypeId);
}
