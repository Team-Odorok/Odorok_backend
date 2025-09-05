package com.odorok.OdorokApplication.mypage.service;

import com.odorok.OdorokApplication.community.repository.ProfileRepository;
import com.odorok.OdorokApplication.course.repository.UserRepository;
import com.odorok.OdorokApplication.domain.User;
import com.odorok.OdorokApplication.draftDomain.Profile;
import com.odorok.OdorokApplication.mypage.dto.request.ProfileUpdateRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MyPageTransactionServiceImpl implements MyPageTransactionService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    @Override
    @Transactional
    public void updateUserProfile(Long id, ProfileUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow();
        user.setNickname(request.getNickName()); //닉네임 변경
        Profile userProfile = profileRepository.findByUserId(id).orElseThrow();
        userProfile.setSidoCode(request.getSidoCode()); //시도코드 변경
        userProfile.setSigunguCode(request.getSigunguCode()); //시군구코드 변경
        userProfile.setDiaryId(request.getDiaryId()); //뭔지 잘 모르겠음 삭제해야 할수도 있다 이거 쓰려면 다이어리 조회기능이 먼저인데?
    }
}
