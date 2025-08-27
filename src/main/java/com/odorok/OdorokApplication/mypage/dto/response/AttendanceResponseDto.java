package com.odorok.OdorokApplication.mypage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceResponseDto {
    List<Integer> days;
    int year;
    int month;
}
