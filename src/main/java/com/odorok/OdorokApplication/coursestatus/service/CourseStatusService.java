package com.odorok.OdorokApplication.coursestatus.service;

public interface CourseStatusService {
    // ===== 외부 제공: 진행거리(미터) =====
    double getTraveledMeters(long courseId, double curLat, double curLon);
}
