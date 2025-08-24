package com.odorok.OdorokApplication.mypage.controller;

import com.odorok.OdorokApplication.commons.response.CommonResponseBuilder;
import com.odorok.OdorokApplication.commons.response.ResponseRoot;
import com.odorok.OdorokApplication.mypage.dto.response.UserInfoResponse;
import com.odorok.OdorokApplication.mypage.service.MyPageService;
import com.odorok.OdorokApplication.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/me")
@RequiredArgsConstructor
@RestController
public class MyPageApiController {
    private final MyPageService myPageService;
    @GetMapping
    public ResponseEntity<ResponseRoot<UserInfoResponse>> searchUserInfo(@AuthenticationPrincipal CustomUserDetails user){
        Long id = user.getUserId();
        UserInfoResponse userInfo = myPageService.findUserInfo(id);
        return ResponseEntity.ok(CommonResponseBuilder.success("유저 정보를 성공적으로 불러왔습니다",userInfo));
    }
}
