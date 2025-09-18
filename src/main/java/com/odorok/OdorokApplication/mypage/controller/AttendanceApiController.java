package com.odorok.OdorokApplication.mypage.controller;


import com.odorok.OdorokApplication.mypage.dto.response.AttendanceResponseDto;
import com.odorok.OdorokApplication.mypage.service.AttendanceService;
import com.odorok.OdorokApplication.security.dto.CustomUserDetails;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.odorok.OdorokApplication.commons.response.CommonResponseBuilder.success;
import static com.odorok.OdorokApplication.commons.response.CommonResponseBuilder.successCreated;

@RequiredArgsConstructor
@RequestMapping("/api/attendances")
@RestController
@Slf4j
public class AttendanceApiController {
    private final AttendanceService attendanceService;

    private final String insertAttendanceSuccessMessage = "출석체크 요청 성공";
    private final String findAttendanceSuccessMessage = "사용자 출석 일자 요청 성공";

    @PostMapping()
    public ResponseEntity<?> registTodayAttendance (@AuthenticationPrincipal CustomUserDetails user) {
        log.debug("Request to /api/attendances for registration by user: {}", user.getUserId());
        attendanceService.insertTodayAttendance(user.getUserId());
        ResponseEntity<?> response = ResponseEntity.status(HttpStatus.CREATED).body(successCreated(insertAttendanceSuccessMessage, null));
        log.debug("Response from /api/attendances for registration: {}", response.getBody());
        return response;
    }

    @GetMapping()
    public ResponseEntity<?> searchAttendanceByMonth (@RequestParam @Min(1) @Max(9999) int year,
                                                      @RequestParam @Min(1) @Max(12) int month,
                                                      @AuthenticationPrincipal CustomUserDetails user) {
        log.debug("Request to /api/attendances for search with year: {}, month: {}", year, month);
        AttendanceResponseDto data = attendanceService.findAttendanceInfoByMonth(user.getUserId(), year, month);
        ResponseEntity<?> response = ResponseEntity.status(HttpStatus.OK).body(success(findAttendanceSuccessMessage, data));
        log.debug("Response from /api/attendances for search: {}", response.getBody());
        return response;
    }

}
