package com.odorok.OdorokApplication.security.config;

import com.odorok.OdorokApplication.security.jwt.filter.JWTAuthenticationFilter;
import com.odorok.OdorokApplication.security.jwt.filter.JWTVerificationFilter;
import com.odorok.OdorokApplication.security.jwt.filter.SecurityExceptionHandlingFilter;
import com.odorok.OdorokApplication.security.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class APISecurityConfig {
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    @Order(1)
    SecurityFilterChain apiSecurityFilterChain(HttpSecurity http, CustomUserDetailsService userDetailsService,
                                               JWTAuthenticationFilter authFilter, JWTVerificationFilter jwtVerifyFilter,
                                               @Qualifier("corsConfigurationSource") CorsConfigurationSource corsConfig,
                                               SecurityExceptionHandlingFilter exceptionFilter) throws Exception {
        http.securityMatcher("/api/**")
                .cors(t -> t.configurationSource(corsConfig))
                .userDetailsService(userDetailsService)
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth -> auth
//                                .requestMatchers(HttpMethod.GET, "/api/auth/**").permitAll() // 인증없이 사용할 경로 등록하기
//                                .requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers("/api/diaries/**").authenticated()
                                .anyRequest().permitAll())
                .addFilterBefore(jwtVerifyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(authFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(exceptionFilter, JWTVerificationFilter.class);
        return http.build();
    }
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        //  모든 도메인(origin) 허용 -> 운영 시 특정 도메인으로 변경
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }

}
