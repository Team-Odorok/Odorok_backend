package com.odorok.OdorokApplication.mypage.controller;

import com.odorok.OdorokApplication.commons.response.CommonResponseBuilder;
import com.odorok.OdorokApplication.commons.response.ResponseRoot;
import com.odorok.OdorokApplication.community.dto.request.ArticleUpdateRequest;
import com.odorok.OdorokApplication.mypage.dto.request.HealthProfileUpdateRequest;
import com.odorok.OdorokApplication.mypage.dto.request.ProfileUpdateRequest;
import com.odorok.OdorokApplication.mypage.dto.response.UserHealthInfoResponse;
import com.odorok.OdorokApplication.mypage.dto.response.UserInfoResponse;
import com.odorok.OdorokApplication.mypage.dto.response.UserStatisticResponse;
import com.odorok.OdorokApplication.mypage.service.MyPageService;
import com.odorok.OdorokApplication.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/api/me")
@RequiredArgsConstructor
@RestController
@Slf4j
public class MyPageApiController {
    private final MyPageService myPageService;

    @Operation(summary = "유저 프로필 조회", description = "자신의 정보를 조회가능")
    @ApiResponse(responseCode = "200", description = "조회 성공시 정보들이 전송됨")
    @GetMapping("/profile")
    public ResponseEntity<ResponseRoot<UserInfoResponse>> searchUserInfo(@AuthenticationPrincipal CustomUserDetails user){
        log.debug("Request to /api/me/profile for user: {}", user.getUserId());
        Long id = user.getUserId();
        UserInfoResponse userInfo = myPageService.findUserInfo(id);
        ResponseEntity<ResponseRoot<UserInfoResponse>> response = ResponseEntity.ok(CommonResponseBuilder.success("유저 정보를 성공적으로 불러왔습니다",userInfo));
        log.debug("Response from /api/me/profile: {}", response.getBody());
        return response;
    }
    @Operation(summary = "유저 프로필 수정", description = "자신의 정보를 수정가능")
    @ApiResponse(responseCode = "200", description = "수정 성공시 수정 성공 메시지가 전송됨")
    @PutMapping("/profile")
    public ResponseEntity<ResponseRoot<Void>> updateUserProfile(@RequestPart(name = "data") ProfileUpdateRequest request,
                                                                @RequestPart(name = "images") List<MultipartFile> images,
                                                                @AuthenticationPrincipal CustomUserDetails user){
        log.debug("Request to /api/me/profile for update with request: {}, images count: {}", request, images.size());
        Long id = user.getUserId();
        myPageService.updateUserProfile(id,request,images);
        ResponseEntity<ResponseRoot<Void>> response = ResponseEntity.ok(CommonResponseBuilder.success("유저 정보를 성공적으로 변경했습니다"));
        log.debug("Response from /api/me/profile for update: {}", response.getBody());
        return response;
    }
    @Operation(summary = "유저 건강정보 조회", description = "유저 건강정보 조회가능")
    @ApiResponse(responseCode = "200", description = "유저의 건강정보 반환됨")
    @GetMapping("/userhealth")
    public ResponseEntity<ResponseRoot<UserHealthInfoResponse>> searchUserHealthInfo(@AuthenticationPrincipal CustomUserDetails user){
        log.debug("Request to /api/me/userhealth for user: {}", user.getUserId());
        Long id = user.getUserId();
        UserHealthInfoResponse userHealthInfoResponse = myPageService.findUserHealthInfo(id);
        ResponseEntity<ResponseRoot<UserHealthInfoResponse>> response = ResponseEntity.ok(CommonResponseBuilder.success("유저 건강정보 성공적으로 반환",userHealthInfoResponse));
        log.debug("Response from /api/me/userhealth: {}", response.getBody());
        return response;
    }
    @Operation(summary = "유저 건강정보 수정", description = "유저 건강정보 수정가능")
    @ApiResponse(responseCode = "200", description = "유저의 건강정보 수정됨")
    @PutMapping("/userhealth")
    public ResponseEntity<ResponseRoot<Void>> updateUserHealthInfo(@AuthenticationPrincipal CustomUserDetails user,
                                                                   @RequestBody HealthProfileUpdateRequest healthProfileUpdateRequest){
        log.debug("Request to /api/me/userhealth for update with request: {}", healthProfileUpdateRequest);
        Long id = user.getUserId();
        myPageService.updateUserHealthInfo(id,healthProfileUpdateRequest);
        ResponseEntity<ResponseRoot<Void>> response = ResponseEntity.ok(CommonResponseBuilder.success("유저 건강정보 성공적으로 수정"));
        log.debug("Response from /api/me/userhealth for update: {}", response.getBody());
        return response;
    }
    //활동 건수 조회 get
    //마이페이지 다이어리 조회 기능(어떤 값들이 있어야 하는가) <- 모르니까 일단 만들어놓고 값은 그때그때 추가하는 방식
    @Operation(summary = "유저 활동내역 통계", description = "유저 활동내역에 대한 건수")
    @ApiResponse(responseCode = "200", description = "유저의 활동내역 건수 반환")
    @GetMapping("/activity/statistics")
    public ResponseEntity<ResponseRoot<UserStatisticResponse>> searchUserStatistics(@AuthenticationPrincipal CustomUserDetails user){
        log.debug("Request to /api/me/activity/statistics for user: {}", user.getUserId());
        Long id = user.getUserId();
        UserStatisticResponse responseBody = myPageService.searchUserStatistics(id);
        ResponseEntity<ResponseRoot<UserStatisticResponse>> response = ResponseEntity.ok(CommonResponseBuilder.success("유저 통계내역 반환",responseBody));
        log.debug("Response from /api/me/activity/statistics: {}", response.getBody());
        return response;
    }
}
