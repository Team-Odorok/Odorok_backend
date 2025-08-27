package com.odorok.OdorokApplication.mypage.service;


import com.odorok.OdorokApplication.commons.exception.AlreadyCheckedInException;
import com.odorok.OdorokApplication.domain.AttendanceHistory;
import com.odorok.OdorokApplication.mypage.dto.response.AttendanceResponseDto;
import com.odorok.OdorokApplication.mypage.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AttendanceServiceImpl implements AttendanceService{
    private final AttendanceRepository attendanceRepository;

    @Override
    public void insertTodayAttendance(Long userId) {
        AttendanceHistory attendanceHistory = AttendanceHistory.builder()
                .attendedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .userId(userId)
                .build();

        try {
            attendanceRepository.save(attendanceHistory);
        } catch(DataIntegrityViolationException e) {
            throw new AlreadyCheckedInException();
        }
    }

    @Override
    public AttendanceResponseDto findAttendanceInfoByMonth(Long userId, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();

        List<Integer> days = attendanceRepository
                .findAttendedDaysByMonth(userId, start, end);

        return AttendanceResponseDto.builder()
                .year(year)
                .month(month)
                .days(days)
                .build();
    }
}
