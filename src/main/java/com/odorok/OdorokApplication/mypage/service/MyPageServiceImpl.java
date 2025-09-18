package com.odorok.OdorokApplication.mypage.service;

import com.odorok.OdorokApplication.community.repository.ArticleRepository;
import com.odorok.OdorokApplication.community.repository.DiseaseRepository;
import com.odorok.OdorokApplication.community.repository.ProfileRepository;
import com.odorok.OdorokApplication.course.repository.UserDiseaseRepository;
import com.odorok.OdorokApplication.course.repository.UserRepository;
import com.odorok.OdorokApplication.diary.repository.DiaryRepository;
import com.odorok.OdorokApplication.diary.repository.VisitedCourseRepository;
import com.odorok.OdorokApplication.domain.HealthInfo;
import com.odorok.OdorokApplication.domain.User;
import com.odorok.OdorokApplication.domain.UserDisease;
import com.odorok.OdorokApplication.draftDomain.Profile;
import com.odorok.OdorokApplication.draftDomain.Tier;
import com.odorok.OdorokApplication.mypage.dto.request.HealthProfileUpdateRequest;
import com.odorok.OdorokApplication.mypage.dto.request.ProfileInsertRequest;
import com.odorok.OdorokApplication.mypage.dto.request.ProfileUpdateRequest;
import com.odorok.OdorokApplication.mypage.dto.response.UserHealthInfoResponse;
import com.odorok.OdorokApplication.mypage.dto.response.UserInfoResponse;
import com.odorok.OdorokApplication.mypage.dto.response.UserStatisticResponse;
import com.odorok.OdorokApplication.mypage.repository.AttendanceRepository;
import com.odorok.OdorokApplication.mypage.repository.HealthInfoRepository;
import com.odorok.OdorokApplication.mypage.repository.TierRepository;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MyPageServiceImpl implements MyPageService{
    private final ProfileRepository profileRepository;
    private final MyPageImageService myPageImageService;
    private final MyPageTransactionService myPageTransactionService;
    private final HealthInfoRepository healthInfoRepository;
    private final UserDiseaseRepository userDiseaseRepository;
    private final ArticleRepository articleRepository;
    private final DiaryRepository diaryRepository;
    private final VisitedCourseRepository visitedCourseRepository;
    private final AttendanceRepository attendanceRepository;
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
            if(!beforeImgUrl.equals("")||beforeImgUrl!=null) {
                myPageImageService.deleteImages(List.of(beforeImgUrl));
            }
            //이미지 추가
            List<String> imgUrlList = myPageImageService.insertProfileImage(id,images);
            //프로필 변경
            userProfile.setImgUrl(imgUrlList.get(0));
            profileRepository.save(userProfile);
        }
        //이후 트랜잭셔널하게 유저와 프로필의 정보를 변경해줄 것
        myPageTransactionService.updateUserProfile(id,request);
    }

    @Override
    public void insertUserProfile(Long id, ProfileInsertRequest request, List<MultipartFile> images) {
        List<String> imgUrlList = null;
        if(images!=null&&images.size()==1) {
            imgUrlList = myPageImageService.insertProfileImage(id, images);
        }
        Profile userProfile = Profile.builder().userId(id).activityPoint(0)
                .mileage(0).imgUrl("").msgFrequency(request.getMsgFrequency()).msgAgree(request.getMsgAgree()).
                sidoCode(request.getSidoCode()).sigunguCode(request.getSigunguCode()).tierId(1L).build();
        //이미지 추가
        if(imgUrlList!=null) {
            userProfile.setImgUrl(imgUrlList.get(0));
        }
        profileRepository.save(userProfile);

    }

    @Override
    public UserHealthInfoResponse findUserHealthInfo(Long id) {
        HealthInfo healthInfo = healthInfoRepository.findByUserId(id);
        List<Long> diseaseIds = Optional.ofNullable(userDiseaseRepository.findAllDiseaseIdByUserId(id))
                .orElseGet(java.util.List::of);
        return UserHealthInfoResponse.builder().gender(healthInfo.getGender())
                .height(healthInfo.getHeight())
                .weight(healthInfo.getWeight())
                .age(healthInfo.getAge())
                .smoking(healthInfo.getSmoking())
                .drinkPerWeek(healthInfo.getDrinkPerWeek())
                .exercisePerWeek(healthInfo.getExercisePerWeek())
                .diseaseList(diseaseIds)
                .build();
    }

    @Override
    @Transactional
    public void updateUserHealthInfo(Long id, HealthProfileUpdateRequest healthProfileUpdateRequest) {
        HealthInfo healthInfo = healthInfoRepository.findByUserId(id);
        healthInfo.setGender(healthProfileUpdateRequest.getGender());
        healthInfo.setHeight(healthProfileUpdateRequest.getHeight());
        healthInfo.setWeight(healthProfileUpdateRequest.getWeight());
        healthInfo.setAge(healthProfileUpdateRequest.getAge());
        healthInfo.setSmoking(healthProfileUpdateRequest.getSmoking());
        healthInfo.setDrinkPerWeek(healthProfileUpdateRequest.getDrinkPerWeek());
        healthInfo.setExercisePerWeek(healthProfileUpdateRequest.getExercisePerWeek());
        userDiseaseRepository.deleteAllByUserId(id);
        if(healthProfileUpdateRequest.getDiseaseList()!=null) {
           for(Long i : healthProfileUpdateRequest.getDiseaseList()){
                UserDisease userDisease = UserDisease.builder()
                        .diseaseId(i).userId(id).createdAt(LocalDateTime.now())
                        .build();
                userDiseaseRepository.save(userDisease);
           }
        }
    }

    @Override
    @Transactional
    public UserStatisticResponse searchUserStatistics(Long id) {
        //작성글의 좋아요 값을 list로 반환해서 작업
        //나의 모든 오도록은 diary테이블을 이용하여 작업
        //방문코스테이블에서 distance값들만 list로 반환해서 작업
        //attend테이블에서 유저id로 받아와서 작업
        List<Integer> likeList = articleRepository.findAllLikeByUserId(id);
        Integer diaryCount = diaryRepository.countByUserId(id);
        List<Integer> distanceList = visitedCourseRepository.findAllDistanceByUserId(id);
        Integer attendanceCount = attendanceRepository.countByUserId(id);
        Integer likeSum = 0;
        for(Integer i : likeList){
            likeSum += i;
        }
        Integer distanceSum = 0;
        for(Integer i : distanceList){
            distanceSum += i;
        }
        UserStatisticResponse response = UserStatisticResponse.builder()
                .myPostCount(likeList.size())
                .likeCount(likeSum)
                .diaryCount(diaryCount)
                .attendanceCount(attendanceCount)
                .distanceCount(distanceSum)
                .courseCount(distanceList.size())
                .build();
        return response;
    }
}
