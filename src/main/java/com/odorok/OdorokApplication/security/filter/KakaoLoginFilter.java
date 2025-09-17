package com.odorok.OdorokApplication.security.filter;

import com.odorok.OdorokApplication.course.repository.UserRepository;
import com.odorok.OdorokApplication.domain.User;
import com.odorok.OdorokApplication.security.dto.KakaoUserInfoResponse;
import com.odorok.OdorokApplication.security.dto.SignupRequest;
import com.odorok.OdorokApplication.security.exception.EmailNotFoundException;
import com.odorok.OdorokApplication.security.jwt.JWTUtil;
import com.odorok.OdorokApplication.security.service.AuthService;
import com.odorok.OdorokApplication.security.service.SignupService;
import com.odorok.OdorokApplication.security.service.UserQueryService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class KakaoLoginFilter extends OncePerRequestFilter {
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    String redirectUrl;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    String clientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao.scope}")
    String[] scope;

    @Value("${spring.security.oauth2.client.registration.kakao.authorization-grant-type}")
    String authorizationGrantType;

    @Value("${spring.security.oauth2.client.registration.kakao.password-secret}")
    String passwordSecret;

    @Value("${spring.security.oauth2.client.provider.kakao.authorization-uri}")
    String authorizationUri;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    String tokenUri;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    String userInfoUri;

    @Value("${spring.jwt.expiration}")
    private long ACCESS_TOKEN_EXPIRATION;
    @Value("${spring.jwt.refresh-token.expiration}")
    private long REFRESH_TOKEN_EXPIRATION;

    private final UserQueryService userQueryService;
    private final AuthService authService;
    private final JWTUtil jwtUtil;
    private final WebClient client;
    private final UserRepository userRepository;

    public KakaoLoginFilter(UserQueryService userQueryService, AuthService authService, JWTUtil jwtUtil, @Qualifier("normalClient") WebClient client, UserRepository userRepository) {
        this.userQueryService = userQueryService;
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.client = client;
        this.userRepository = userRepository;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 로그인 요청이라면 리다이렉트 시켜줌.
        if(request.getRequestURI().equals("/api/auth/oauth2/login/kakao")) {
            log.debug("[KAKAO login] OAuth2 로그인 요청이 리다이렉트 됩니다.");
            response.sendRedirect(this.getKakaoAuthServerUrl());
            return;
        }

        // 사용자 정보를 받아오기 위해 Authorization 서버에 액세스 토큰을 요청함.
        // 인증, 동의 후 리다이렉트된다면, 이후 code가 포함된 요청이 옴.
        // 아니라면 pass
        if(!request.getRequestURI().equals("/api/auth/oauth2/kakao")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 요청 파라미터에서 code 추출.
        String code = request.getParameter("code");
        // code가 없다면, 에러코드 리턴.
        if(code == null) {
            response.setStatus(403);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("text/plain;charset=UTF-8");
            response.getOutputStream().write("[KAKAO login] 인증서버로부터 code를 받아오지 못했습니다..".getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().flush();
            return;
        }

        log.debug("[KAKAO login] 카카오 인증을 위해 token URL로 액세스 토큰을 요청함.");

        Map result = client.post().uri(tokenUri).contentType(MediaType.APPLICATION_FORM_URLENCODED).body(BodyInserters
                        .fromFormData("grant_type", authorizationGrantType)
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("code", code)
                        .with("redirect_uri", redirectUrl))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        String accessTokenForKakao = (String)result.get("access_token");
        
        if(accessTokenForKakao == null || accessTokenForKakao.isEmpty()) {
            response.setStatus(400);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("text/plain;charset=UTF-8");
            response.getOutputStream().write("[KAKAO login] 인증 서버로부터 액세스토큰을 받아오지 못했습니다.".getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().flush();
            return;
        }
        
        // 발급받은 액세스 토큰을 사용해 로그인한 사용자의 정보를 조회함.

        KakaoUserInfoResponse userInfo = client.get().uri(userInfoUri).headers(
                httpHeaders ->
                        httpHeaders.add("Authorization", "Bearer "+accessTokenForKakao)
        ).retrieve().bodyToMono(KakaoUserInfoResponse.class).block();

        if(userInfo == null) {
            response.setStatus(400);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("text/plain;charset=UTF-8");
            response.getOutputStream().write("[KAKAO login] 리소스 서버로부터 유저정보를 받아오지 못했습니다.".getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().flush();
            return;
        }
        
        String email = userInfo.getSafeEmail();
        if(email == null || email.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("text/plain;charset=UTF-8");
            response.getOutputStream().write("[KAKAO login] 이메일이 존재하지 않는 카카오 유저입니다.".getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().flush();
            return; // <- filterChain.doFilter 호출 금지
        }
        // 유저 정보가 DB에 있는지 체크한다.

        log.debug("[KAKAO login] 카카오로부터 읽어온 사용자 이메일 = {}", email);

        String nickname = userInfo.getSafeNickname();

        // 카카오 유저의 인증 과정에는 카카오용 시크릿 값을 사용하자.
        // 카카오톡 로그인 유저 정보가 회원테이블에 없을 경우, 회원가입을 진행한다.
        User user = null;
        if(!userQueryService.existsByEmail(email)) {
            user = new User(null, null, nickname, email, passwordSecret, "ROLE_USER");
            userRepository.save(user);
        }
        else {
            // 회원정보를 불러온다.
            user = userQueryService.queryUserByEmail(email);
        }
        // 토큰을 만든다.
        // 모든 토큰 무효화
        authService.invalidateAllTokens(email);
        String accessToken = jwtUtil.createJwt(email, user.getRole(), ACCESS_TOKEN_EXPIRATION);
        String refreshToken = jwtUtil.createJwt(email, user.getRole(), REFRESH_TOKEN_EXPIRATION);

        authService.issueNewToken(accessToken, email, "ACCESS");
        authService.issueNewToken(refreshToken, email, "REFRESH");

        response.addHeader("Authorization", "Bearer "+ accessToken);

        Cookie cookie = new Cookie("refresh-token", refreshToken);
        // 밀리초를 초단위로.
        cookie.setMaxAge((int)(REFRESH_TOKEN_EXPIRATION / 1000));
        cookie.setPath("/");
        response.addCookie(cookie);

        response.setStatus(200);
        return;
    }

    private String getKakaoAuthServerUrl() {
        StringBuffer sb = new StringBuffer(authorizationUri);
        sb.append("?client_id=").append(clientId).append("&redirect_uri=").append(redirectUrl);
        sb.append("&response_type=code");
        if(scope != null) {
            sb.append("&scope=").append(String.join(",", scope));
        }
        return sb.toString();
    }


}
