package com.odorok.OdorokApplication.mypage.service;

import com.odorok.OdorokApplication.mypage.dto.response.AttendanceResponseDto;

public interface AttendanceService {
    void insertTodayAttendance(Long userId);
    AttendanceResponseDto findAttendanceInfoByMonth(Long userId, int year, int month);
}
