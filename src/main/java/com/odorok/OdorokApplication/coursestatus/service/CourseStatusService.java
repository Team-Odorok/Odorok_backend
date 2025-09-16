package com.odorok.OdorokApplication.coursestatus.service;

import com.odorok.OdorokApplication.coursestatus.dto.dto.TravelProgress;

public interface CourseStatusService {
    // ===== 외부 제공: 진행거리(미터) =====

    // ===== 외부 제공: 진행거리(미터) =====
    TravelProgress getTraveledProgress(long courseId, double curLat, double curLon);

    void registCourseStatus(Long userId, Long courseId);

    Long findProgress(Long userId, Long courseId, Double curlatitude, Double curlongitude);

    void updateCourseStatus(Long userId, Long courseId, Double curlatitude, Double curlongitude);
}
