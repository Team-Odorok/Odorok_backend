package com.odorok.OdorokApplication.security.controller;

import com.odorok.OdorokApplication.commons.response.CommonResponseBuilder;
import com.odorok.OdorokApplication.commons.response.ResponseRoot;
import com.odorok.OdorokApplication.security.dto.SignupRequest;
import com.odorok.OdorokApplication.security.exception.DuplicateUserEmailException;
import com.odorok.OdorokApplication.security.exception.JWTTokenExpiredException;
import com.odorok.OdorokApplication.security.service.AuthService;
import com.odorok.OdorokApplication.security.service.SignupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원가입 컨트롤러", description = "회원가입 API 엔드포인트")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final SignupService signupService;
    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "이메일, 비밀변호, 닉네임을 전송하여 회원가입합니다.")
    @ApiResponse(responseCode = "201", description = "회원가입 성공")
    @ApiResponse(responseCode = "400", description = "중복 아이디 발견")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            signupService.signup(request);
        } catch(DuplicateUserEmailException e) {
            log.debug("에러발생 : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonResponseBuilder.fail(e.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponseBuilder.successCreated("회원가입이 완료되었습니다.", null));
    }

    @GetMapping("/refresh-token")
    @Operation(summary = "액세스 토큰 재발행", description = "액세스 토큰 만료시 리프레시 토큰으로 재발행합니다.")
    @ApiResponse(responseCode = "201", description = "액세스 토큰 재생성 성공")
    public ResponseEntity<?> getTokenRefresing(HttpServletRequest request, HttpServletResponse response) {
        try {
            authService.refresh(request, response);
        } catch (JWTTokenExpiredException e) {
            log.debug("액세스 토큰 재발행 실패 : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonResponseBuilder.fail(e.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(CommonResponseBuilder.successCreated("액세스 토큰 재발행 성공.", null));
    }
}
