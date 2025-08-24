package com.odorok.OdorokApplication.security.config;

import com.odorok.OdorokApplication.security.filter.CustomLoginFilter;
import com.odorok.OdorokApplication.security.filter.JWTFilter;
import com.odorok.OdorokApplication.security.jwt.JWTUtil;
import com.odorok.OdorokApplication.security.repository.AuthUserRepository;
import com.odorok.OdorokApplication.security.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Value("${spring.jwt.expiration}")
    private long ACCESS_TOKEN_EXPIRATION;
    @Value("${spring.jwt.refresh-token.expiration}")
    private long REFRESH_TOKEN_EXPIRATION;

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final AuthService authService;
    private final AuthUserRepository authUserRepository;

    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(auth -> auth.disable());
        http.cors(cors -> cors
                .configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration configuration = new CorsConfiguration();
                        // 허용할 오리진 설정
                        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
                        // 허용할 메서드 설정
                        configuration.setAllowedMethods(Collections.singletonList("*"));
                        // 프론트엔드에서 Credential 설정을 하면
                        configuration.setAllowCredentials(true);
                        // 허용할 헤더
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        // 허용할 시간
                        configuration.setMaxAge(3600L);

                        // 응답의 Authorization 헤더가 토큰을 포함하니까 노출하도록 허용.
                        configuration.setExposedHeaders(Collections.singletonList("Authorization"));

                        return configuration;
                    }
                }));

        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        // 도달시 SecurityContext의 여부와 권한을 검증할지를 설정한다.
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**",
                        "/api/attractions/**",
                        "/api/regions/**",
                        "/api/courses",
                        "/api/courses/detail",
                        "/api/courses/region",
                        "/api/courses/top",
                        "/h2-console/**",
                        "/odorokapi-ui.html",
                        "/swagger-ui/**", // swagger-ui 관련 모든 경로 허용
                        "/api-docs/json/**",  // openapi v3 문서 경로 허용
                        "/swagger-resources/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated());

        http.formLogin(auth -> auth.disable());

        // 커스텀 필터를 addFilterAt 해줘야 함.
        CustomLoginFilter customLoginFilter = new CustomLoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, ACCESS_TOKEN_EXPIRATION, REFRESH_TOKEN_EXPIRATION, authService);
        customLoginFilter.setFilterProcessesUrl("/api/auth/login");

        http.addFilterAt(
                customLoginFilter,
                UsernamePasswordAuthenticationFilter.class
        );


        // 인증 필터에서 통과시킬 경로를 설정한다.
        Set<String> allowedUris = new HashSet<>();
        String[] uris = {"/api/courses",
                "/api/refresh-token",
                "/api/courses/detail",
                "/api/courses/region",
                "/api/courses/top",
                "/signup",
                "/h2-console/**",
                "/odorokapi-ui.html",
                "/swagger-ui/**", // swagger-ui 관련 모든 경로 허용
                "/api-docs/json/**",  // openapi v3 문서 경로 허용
                "/swagger-resources/**"};

        Collections.addAll(allowedUris, uris);


        JWTFilter jwtFilter = new JWTFilter(jwtUtil, allowedUris, authService, authUserRepository);
        http.addFilterBefore(jwtFilter, CustomLoginFilter.class);

        // 가장 중요한 처리 : 세션을 생성하지 않도록 함.
        http.sessionManagement(auth -> auth.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 액세스 토큰 블랙 리스트를 만들기 위한 로그아웃 정의
        http.logout(auth -> auth.logoutUrl("/api/auth/logout")
                .addLogoutHandler(authService)
                .logoutSuccessHandler((request, response, authentication) ->
                {
                    // 존재하는 토큰을 삭제해야 한다.
                    // 로그인 할 때, DB에 액세스 토큰이든 리프레시 토큰이든 저장하자.
                    // 여기서 이제 응답에 포함되는 토큰을 제거해야함.
                    response.setHeader("Authorization", "");
                    Cookie[] cookies = request.getCookies();
                    for (Cookie cookie : cookies) {
                        if (cookie.getName().equals("refresh-token")) continue;
                        response.addCookie(cookie);
                    }
                    response.setStatus(HttpStatus.OK.value());
                }));

        return http.build();
    }
}

