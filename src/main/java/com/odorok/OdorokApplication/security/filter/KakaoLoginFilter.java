package com.odorok.OdorokApplication.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Map;

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


    @Value("${spring.security.oauth2.client.provider.kakao.authorization-uri}")
    String authorizationUri;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    String tokenUri;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    String userInfoUri;


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
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("[KAKAO login] 카카오 인증을 위해 token URL로 액세스 토큰을 요청함.");
        WebClient client = WebClient.builder().build();
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

        // 발급받은 액세스 토큰을 사용해 로그인한 사용자의 정보를 조회함.
        WebClient usrInfoClient = WebClient.builder().build();
        String userInfo = usrInfoClient.get().uri(userInfoUri).headers(
                httpHeaders ->
                        httpHeaders.add("Authorization", "Bearer "+accessTokenForKakao)
        ).retrieve().bodyToMono(String.class).block();
        
        log.debug("[KAKAO login] 카카오로부터 읽어온 사용자의 정보 = {}", userInfo);

        filterChain.doFilter(request, response);
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
