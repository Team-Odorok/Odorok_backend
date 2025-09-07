package com.odorok.OdorokApplication.mypage.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ProfileUpdateRequest {
    private String nickName;
    private Integer sidoCode;
    private Integer sigunguCode;
    private Long diaryId;
}
