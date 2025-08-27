package com.odorok.OdorokApplication.mypage.controller;


import com.odorok.OdorokApplication.mypage.dto.response.AttendanceResponseDto;
import com.odorok.OdorokApplication.mypage.service.AttendanceService;
import com.odorok.OdorokApplication.security.dto.CustomUserDetails;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.odorok.OdorokApplication.commons.response.CommonResponseBuilder.success;
import static com.odorok.OdorokApplication.commons.response.CommonResponseBuilder.successCreated;

@RequiredArgsConstructor
@RequestMapping("/api/attendances")
@RestController
public class AttendanceApiController {
    private final AttendanceService attendanceService;

    private final String insertAttendanceSuccessMessage = "출석체크 요청 성공";
    private final String findAttendanceSuccessMessage = "사용자 출석 일자 요청 성공";

    @PostMapping()
    public ResponseEntity<?> registTodayAttendance (@AuthenticationPrincipal CustomUserDetails user) {
        attendanceService.insertTodayAttendance(user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(successCreated(insertAttendanceSuccessMessage, null));
    }

    @GetMapping()
    public ResponseEntity<?> searchAttendanceByMonth (@RequestParam @Min(1) @Max(9999) int year,
                                                      @RequestParam @Min(1) @Max(12) int month,
                                                      @AuthenticationPrincipal CustomUserDetails user) {
        AttendanceResponseDto data = attendanceService.findAttendanceInfoByMonth(user.getUserId(), year, month);
        return ResponseEntity.status(HttpStatus.OK).body(success(findAttendanceSuccessMessage, data));
    }








}
