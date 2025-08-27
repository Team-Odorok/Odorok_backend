package com.odorok.OdorokApplication.mypage.repository;

import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceRepositoryCustom {
    List<Integer> findAttendedDaysByMonth(Long userId, LocalDateTime start, LocalDateTime end);
}
