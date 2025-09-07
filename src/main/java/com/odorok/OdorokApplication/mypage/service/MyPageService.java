package com.odorok.OdorokApplication.mypage.service;

import com.odorok.OdorokApplication.mypage.dto.request.HealthProfileUpdateRequest;
import com.odorok.OdorokApplication.mypage.dto.request.ProfileUpdateRequest;
import com.odorok.OdorokApplication.mypage.dto.response.UserHealthInfoResponse;
import com.odorok.OdorokApplication.mypage.dto.response.UserInfoResponse;
import com.odorok.OdorokApplication.mypage.dto.response.UserStatisticResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MyPageService {
    //user id를 통해서 유저 이름,티어,활동점수,프로필url을 받음
    public UserInfoResponse findUserInfo(Long id);
    public void updateUserProfile(Long id, ProfileUpdateRequest request, List<MultipartFile> images);
    UserHealthInfoResponse findUserHealthInfo(Long id);
    void updateUserHealthInfo(Long id, HealthProfileUpdateRequest healthProfileUpdateRequest);

    UserStatisticResponse searchUserStatistics(Long id);
}
