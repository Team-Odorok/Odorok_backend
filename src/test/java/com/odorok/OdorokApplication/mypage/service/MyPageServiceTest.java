package com.odorok.OdorokApplication.mypage.service;

import com.odorok.OdorokApplication.commons.querydsl.config.QueryDslConfig;
import com.odorok.OdorokApplication.community.repository.ArticleRepository;
import com.odorok.OdorokApplication.community.repository.ProfileRepository;
import com.odorok.OdorokApplication.course.repository.UserDiseaseRepository;
import com.odorok.OdorokApplication.course.repository.UserRepository;
import com.odorok.OdorokApplication.domain.HealthInfo;
import com.odorok.OdorokApplication.domain.User;
import com.odorok.OdorokApplication.domain.UserDisease;
import com.odorok.OdorokApplication.draftDomain.Article;
import com.odorok.OdorokApplication.draftDomain.Profile;
import com.odorok.OdorokApplication.mypage.dto.request.HealthProfileUpdateRequest;
import com.odorok.OdorokApplication.mypage.dto.request.ProfileUpdateRequest;
import com.odorok.OdorokApplication.mypage.dto.response.UserHealthInfoResponse;
import com.odorok.OdorokApplication.mypage.dto.response.UserInfoResponse;
import com.odorok.OdorokApplication.mypage.repository.HealthInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    @Mock
    UserDiseaseRepository userDiseaseRepository;
    @Mock
    HealthInfoRepository healthInfoRepository;
    @InjectMocks
    MyPageServiceImpl myPageService;
    private final Long userId = 42L;
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
    @Test
    void 사용자_건강정보조회_리포지토리값매핑() {
        // given
        HealthInfo hi = mock(HealthInfo.class);
        when(hi.getGender()).thenReturn(Boolean.TRUE);
        when(hi.getHeight()).thenReturn(175.5);
        when(hi.getWeight()).thenReturn(70.2);
        when(hi.getAge()).thenReturn(31);
        when(hi.getSmoking()).thenReturn(Boolean.FALSE);
        when(hi.getDrinkPerWeek()).thenReturn(2);
        when(hi.getExercisePerWeek()).thenReturn(4);

        when(healthInfoRepository.findByUserId(userId)).thenReturn(hi);
        when(userDiseaseRepository.findAllDiseaseIdByUserId(userId))
                .thenReturn(List.of(101L, 202L));

        // when
        UserHealthInfoResponse res = myPageService.findUserHealthInfo(userId);

        // then
        assertThat(res).isNotNull();
        assertThat(res.getGender()).isTrue();
        assertThat(res.getHeight()).isEqualTo(175.5);
        assertThat(res.getWeight()).isEqualTo(70.2);
        assertThat(res.getAge()).isEqualTo(31);
        assertThat(res.getSmoking()).isFalse();
        assertThat(res.getDrinkPerWeek()).isEqualTo(2);
        assertThat(res.getExercisePerWeek()).isEqualTo(4);
        assertThat(res.getDiseaseList()).containsExactly(101L, 202L);

        verify(healthInfoRepository).findByUserId(userId);
        verify(userDiseaseRepository).findAllDiseaseIdByUserId(userId);
        verifyNoMoreInteractions(healthInfoRepository, userDiseaseRepository);
    }

    @Test
    void 사용자_건강정보수정_필드업데이트_질환삭제후삽입() {
        // given
        HealthInfo hi = mock(HealthInfo.class);
        when(healthInfoRepository.findByUserId(userId)).thenReturn(hi);

        HealthProfileUpdateRequest req = mock(HealthProfileUpdateRequest.class);
        when(req.getGender()).thenReturn(Boolean.FALSE);
        when(req.getHeight()).thenReturn(168.0);
        when(req.getWeight()).thenReturn(59.3);
        when(req.getAge()).thenReturn(29);
        when(req.getSmoking()).thenReturn(Boolean.TRUE);
        when(req.getDrinkPerWeek()).thenReturn(3);
        when(req.getExercisePerWeek()).thenReturn(2);
        when(req.getDiseaseList()).thenReturn(List.of(11L, 22L, 33L));

        ArgumentCaptor<UserDisease> captor = ArgumentCaptor.forClass(UserDisease.class);

        // when
        myPageService.updateUserHealthInfo(userId, req);

        // then: HealthInfo 세터 호출 확인 (Boolean/Double 타입 주의)
        verify(hi).setGender(Boolean.FALSE);
        verify(hi).setHeight(168.0);
        verify(hi).setWeight(59.3);
        verify(hi).setAge(29);
        verify(hi).setSmoking(Boolean.TRUE);
        verify(hi).setDrinkPerWeek(3);
        verify(hi).setExercisePerWeek(2);

        // 기존 질환 삭제
        verify(userDiseaseRepository).deleteAllByUserId(userId);

        // 신규 질환 저장 3회 + 필드값 검증
        verify(userDiseaseRepository, times(3)).save(captor.capture());
        List<UserDisease> saved = captor.getAllValues();

        assertThat(saved).hasSize(3);
        assertThat(saved).extracting(UserDisease::getUserId).containsOnly(userId);
        assertThat(saved).extracting(UserDisease::getDiseaseId)
                .containsExactlyInAnyOrder(11L, 22L, 33L);
        assertThat(saved).allSatisfy(ud ->
                assertThat(ud.getCreatedAt()).as("createdAt should be set").isNotNull());

        verify(healthInfoRepository).findByUserId(userId);
        verifyNoMoreInteractions(healthInfoRepository, userDiseaseRepository);
    }

    @Test
    void 사용자_건강정보없을때_NPE발생() {
        // given
        when(healthInfoRepository.findByUserId(userId)).thenReturn(null);
        HealthProfileUpdateRequest req = mock(HealthProfileUpdateRequest.class);

        // when & then (현재 구현은 null 체크 없음 → NPE 발생이 기대동작)
        assertThatThrownBy(() -> myPageService.updateUserHealthInfo(userId, req))
                .isInstanceOf(NullPointerException.class);

        verify(healthInfoRepository).findByUserId(userId);
        verifyNoMoreInteractions(healthInfoRepository, userDiseaseRepository);
    }

}