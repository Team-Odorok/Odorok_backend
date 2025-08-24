package com.odorok.OdorokApplication.community.repository;

import com.odorok.OdorokApplication.mypage.dto.response.UserInfoResponse;

public interface ProfileRepositoryCustom {
    UserInfoResponse findProfileByUserId(Long userId);
}
