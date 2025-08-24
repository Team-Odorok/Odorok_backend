package com.odorok.OdorokApplication.mypage.service;

import com.odorok.OdorokApplication.community.repository.ProfileRepository;
import com.odorok.OdorokApplication.course.repository.UserRepository;
import com.odorok.OdorokApplication.domain.User;
import com.odorok.OdorokApplication.draftDomain.Profile;
import com.odorok.OdorokApplication.draftDomain.Tier;
import com.odorok.OdorokApplication.mypage.dto.response.UserInfoResponse;
import com.odorok.OdorokApplication.mypage.repository.TierRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyPageServiceImpl implements MyPageService{
    private final ProfileRepository profileRepository;
    @Override
    public UserInfoResponse findUserInfo(Long userId) {
        return profileRepository.findProfileByUserId(userId);
    }
}
