package com.odorok.OdorokApplication.mypage.service;

import com.odorok.OdorokApplication.community.repository.ProfileRepository;
import com.odorok.OdorokApplication.course.repository.UserRepository;
import com.odorok.OdorokApplication.domain.User;
import com.odorok.OdorokApplication.draftDomain.Profile;
import com.odorok.OdorokApplication.draftDomain.Tier;
import com.odorok.OdorokApplication.mypage.dto.request.ProfileUpdateRequest;
import com.odorok.OdorokApplication.mypage.dto.response.UserInfoResponse;
import com.odorok.OdorokApplication.mypage.repository.TierRepository;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageServiceImpl implements MyPageService{
    private final ProfileRepository profileRepository;
    private final MyPageImageService myPageImageService;
    private final MyPageTransactionService myPageTransactionService;
    @Override
    public UserInfoResponse findUserInfo(Long userId) {
        return profileRepository.findProfileByUserId(userId);
    }

    @Override
    public void updateUserProfile(Long id, ProfileUpdateRequest request, List<MultipartFile> images) {
        //유저와 프로필 테이블을 불러와서 해당되는 정보를 변경해줄 것
        //만약 이미지가 있다면 이미지 파일을 imageServcie로 올려주고 imgUrl을 컬럼에 적용해줄 것
        if(images!=null&&images.size()==1){
            //이미지 삭제
            Profile userProfile = profileRepository.findByUserId(id).orElseThrow();
            String beforeImgUrl = userProfile.getImgUrl();
            myPageImageService.deleteImages(List.of(beforeImgUrl));
            //이미지 추가
            List<String> imgUrlList = myPageImageService.insertProfileImage(id,images);
            //프로필 변경
            userProfile.setImgUrl(imgUrlList.get(0));
            profileRepository.save(userProfile);
        }
        //이후 트랜잭셔널하게 유저와 프로필의 정보를 변경해줄 것
        myPageTransactionService.updateUserProfile(id,request);
    }
}
