package com.odorok.OdorokApplication.security.service;


import com.odorok.OdorokApplication.community.repository.ProfileRepository;
import com.odorok.OdorokApplication.domain.User;
import com.odorok.OdorokApplication.draftDomain.Profile;
import com.odorok.OdorokApplication.security.dto.SignupRequest;
import com.odorok.OdorokApplication.security.exception.DuplicateUserEmailException;
import com.odorok.OdorokApplication.security.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignupServiceImpl implements SignupService{
    private final AuthUserRepository authUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final ProfileRepository profileRepository;

    @Override
    public void signup(SignupRequest request) {
        User user = User.builder().email(request.getEmail())
                .password(bCryptPasswordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .role("ROLE_USER").build();

        if(authUserRepository.existsByEmail(request.getEmail())) {
            log.debug("이미 존재하는 이메일 입니다. (email : "+request.getEmail() + ")");
            throw new DuplicateUserEmailException("이미 존재하는 이메일 입니다. (email : "+request.getEmail() + ")");
        }


        // 여기 수정함 -> 프로필 생성 시점을 몰라서 회원가입시 프로필 생성하게 함.
        authUserRepository.save(user);
        Profile profile = Profile.builder().userId(user.getId()).mileage(0).activityPoint(0).build();
        profileRepository.save(profile);
    }
}
