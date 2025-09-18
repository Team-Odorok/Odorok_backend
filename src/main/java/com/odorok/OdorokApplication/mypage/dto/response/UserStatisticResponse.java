package com.odorok.OdorokApplication.mypage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@Builder
public class UserStatisticResponse {
    private Integer myPostCount;
    private Integer likeCount;
    private Integer diaryCount;
    private Integer courseCount;
    private Integer distanceCount;
    private Integer attendanceCount;
}
