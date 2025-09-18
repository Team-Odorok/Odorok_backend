package com.odorok.OdorokApplication.mypage.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ProfileInsertRequest {
    private String msgFrequency;
    private Boolean msgAgree;
    private Integer attendanceCount;
    private Integer sidoCode;
    private Integer sigunguCode;
}
