package com.odorok.OdorokApplication.diary.repository;

import com.odorok.OdorokApplication.diary.dto.gpt.VisitedAdditionalAttraction;
import com.odorok.OdorokApplication.diary.dto.gpt.VisitedCourseAndAttraction;

import java.util.List;

public interface VisitedCourseRepositoryCustom {
    List<VisitedAdditionalAttraction> findVisitedAttractionByVisitedCourseId(Long userId, Long visitedCourseId);
    VisitedCourseAndAttraction findCourseAndAttractionsByVisitedCourseId(Long userId, Long visitedCourseId);
}
