package com.odorok.OdorokApplication.mypage.controller;

import com.odorok.OdorokApplication.commons.response.CommonResponseBuilder;
import com.odorok.OdorokApplication.commons.response.ResponseRoot;
import com.odorok.OdorokApplication.community.dto.request.ArticleUpdateRequest;
import com.odorok.OdorokApplication.mypage.dto.request.HealthProfileUpdateRequest;
import com.odorok.OdorokApplication.mypage.dto.request.ProfileUpdateRequest;
import com.odorok.OdorokApplication.mypage.dto.response.UserHealthInfoResponse;
import com.odorok.OdorokApplication.mypage.dto.response.UserInfoResponse;
import com.odorok.OdorokApplication.mypage.service.MyPageService;
import com.odorok.OdorokApplication.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/api/me")
@RequiredArgsConstructor
@RestController
public class MyPageApiController {
    private final MyPageService myPageService;

    @Operation(summary = "유저 프로필 조회", description = "자신의 정보를 조회가능")
    @ApiResponse(responseCode = "200", description = "조회 성공시 정보들이 전송됨")
    @GetMapping("/profile")
    public ResponseEntity<ResponseRoot<UserInfoResponse>> searchUserInfo(@AuthenticationPrincipal CustomUserDetails user){
        Long id = user.getUserId();
        UserInfoResponse userInfo = myPageService.findUserInfo(id);
        return ResponseEntity.ok(CommonResponseBuilder.success("유저 정보를 성공적으로 불러왔습니다",userInfo));
    }
    @Operation(summary = "유저 프로필 수정", description = "자신의 정보를 수정가능")
    @ApiResponse(responseCode = "200", description = "수정 성공시 수정 성공 메시지가 전송됨")
    @PutMapping("/profile")
    public ResponseEntity<ResponseRoot<Void>> updateUserProfile(@RequestPart(name = "data") ProfileUpdateRequest request,
                                                                @RequestPart(name = "images") List<MultipartFile> images,
                                                                @AuthenticationPrincipal CustomUserDetails user){
        //서비스에서 입력된 값을 기반으로 변경해줘야 함
        //변경될 값은 서비스에서 자세히 표시
        //변경 이후에는 성공 메시지를 전송해줘야 함
        //만약 유저가 프로필 이미지를 바꾸고 싶다면 바꿀 수 ImageService를 이용해서 변경할것
        Long id = user.getUserId();
        myPageService.updateUserProfile(id,request,images);
        return ResponseEntity.ok(CommonResponseBuilder.success("유저 정보를 성공적으로 변경했습니다"));
    }
    @Operation(summary = "유저 건강정보 조회", description = "유저 건강정보 조회가능")
    @ApiResponse(responseCode = "200", description = "유저의 건강정보 반환됨")
    @GetMapping("/userhealth")
    public ResponseEntity<ResponseRoot<UserHealthInfoResponse>> searchUserHealthInfo(@AuthenticationPrincipal CustomUserDetails user){
        Long id = user.getUserId();
        UserHealthInfoResponse userHealthInfoResponse = myPageService.findUserHealthInfo(id);
        return ResponseEntity.ok(CommonResponseBuilder.success("유저 건강정보 성공적으로 반환",userHealthInfoResponse));
    }
    @Operation(summary = "유저 건강정보 수정", description = "유저 건강정보 수정가능")
    @ApiResponse(responseCode = "200", description = "유저의 건강정보 수정됨")
    @PutMapping("/userhealth")
    public ResponseEntity<ResponseRoot<Void>> updateUserHealthInfo(@AuthenticationPrincipal CustomUserDetails user,
                                                                   @RequestBody HealthProfileUpdateRequest healthProfileUpdateRequest){
        Long id = user.getUserId();
        myPageService.updateUserHealthInfo(id,healthProfileUpdateRequest);
        return ResponseEntity.ok(CommonResponseBuilder.success("유저 건강정보 성공적으로 수정"));
    }
}
