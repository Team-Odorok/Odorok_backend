package com.odorok.OdorokApplication.mypage.service;

import com.odorok.OdorokApplication.mypage.dto.request.ProfileUpdateRequest;

public interface MyPageTransactionService {

    public void updateUserProfile(Long id, ProfileUpdateRequest request);
}
