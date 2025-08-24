package com.odorok.OdorokApplication.mypage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class UserInfoResponse {
    private String userProfilePictureUrl;
    private String userName;
    private Integer tierLevel;
    private String userTier;
    private Integer userActivityScore;
}
