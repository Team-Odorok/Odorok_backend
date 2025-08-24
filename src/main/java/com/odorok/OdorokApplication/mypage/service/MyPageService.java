package com.odorok.OdorokApplication.mypage.service;

import com.odorok.OdorokApplication.mypage.dto.response.UserInfoResponse;

public interface MyPageService {
    //user id를 통해서 유저 이름,티어,활동점수,프로필url을 받음
    public UserInfoResponse findUserInfo(Long id);
}
