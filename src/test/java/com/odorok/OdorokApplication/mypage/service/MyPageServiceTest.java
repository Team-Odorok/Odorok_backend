package com.odorok.OdorokApplication.mypage.service;

import com.odorok.OdorokApplication.commons.querydsl.config.QueryDslConfig;
import com.odorok.OdorokApplication.community.repository.ArticleRepository;
import com.odorok.OdorokApplication.community.repository.ProfileRepository;
import com.odorok.OdorokApplication.course.repository.UserRepository;
import com.odorok.OdorokApplication.domain.User;
import com.odorok.OdorokApplication.draftDomain.Article;
import com.odorok.OdorokApplication.draftDomain.Profile;
import com.odorok.OdorokApplication.mypage.dto.request.ProfileUpdateRequest;
import com.odorok.OdorokApplication.mypage.dto.response.UserInfoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MyPageServiceTest {
    @Mock
    MyPageImageService myPageImageService;
    @Mock
    UserRepository userRepository;
    @Mock
    MyPageTransactionService myPageTransactionService;
    @Mock
    ProfileRepository profileRepository;
    @InjectMocks
    MyPageServiceImpl myPageService;
    @Test
    void 유저_프로필_조회_로직_성공(){
        //given
        UserInfoResponse userInfoResponse = new UserInfoResponse("ss","ss",1,"ss",10);
        //when
        when(profileRepository.findProfileByUserId(1L)).thenReturn(userInfoResponse);

        UserInfoResponse response = myPageService.findUserInfo(1L);
        assertEquals(response.getUserTier(),userInfoResponse.getUserTier());
    }

    //수정은 Long id, ProfileUpdateRequest request, List<MultipartFile> images인자를 받음
    @Test
    void 유저_프로필_수정_성공_이미지_존재(){
        //images가 null이라면?
        //given
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        List<MultipartFile> list = List.of(
                new MockMultipartFile(
                        "file",                 // form field name
                        "test.png",             // 원본 파일명
                        "image/png",            // content type
                        "dummy image".getBytes() // 파일 내용 (바이트 배열)
                )
        );
        List<String> imgUrlList = List.of("imgurl");
        Profile profile = Profile.builder().imgUrl("imgUrl").build();
        //when
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(myPageImageService.insertProfileImage(1L,list)).thenReturn(imgUrlList);
        //then
        myPageService.updateUserProfile(1L,request,list);
        //s3인서트 작업이 일어났는가
        verify(myPageImageService,times(1)).insertProfileImage(1L,list);
        verify(myPageTransactionService,times(1)).updateUserProfile(1L,request);
    }
    @Test
    void 유저_프로필_수정_성공_이미지_존재X(){
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        List<MultipartFile> list = null;
        //then
        myPageService.updateUserProfile(1L,request,list);
        //s3인서트 작업이 일어나지 않아야함
        verify(myPageImageService,times(0)).insertProfileImage(1L,list);
        verify(myPageTransactionService,times(1)).updateUserProfile(1L,request);
    }
}