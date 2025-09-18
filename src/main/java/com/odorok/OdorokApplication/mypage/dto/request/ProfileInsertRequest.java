package com.odorok.OdorokApplication.mypage.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ProfileInsertRequest {
    private Long userId;
    private Long activityPoint;
    private Long mileage;
    private String imgUrl;
    private String msgFrequency;
    private Boolean msgAgree;
    private Integer attendanceCount;
    private Integer sidoCode;
    private Integer sigunguCode;
    private Long diaryId;
}
