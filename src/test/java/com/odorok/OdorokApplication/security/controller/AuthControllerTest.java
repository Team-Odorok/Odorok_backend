package com.odorok.OdorokApplication.security.controller;

import com.odorok.OdorokApplication.course.repository.UserRepository;
import com.odorok.OdorokApplication.security.dto.SignupRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerTest {
    @LocalServerPort
    private long port;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void 회원가입_성공() {
        SignupRequest request = new SignupRequest("dnjswns648@naver.com", "1cczEE!3", "LWJ");
        TestRestTemplate restTemplate = new TestRestTemplate();
        restTemplate.postForObject("http://localhost:" + port + "/api/auth/signup", request, Void.class);

        assertThat(userRepository.findByEmail("dnjswns648@naver.com").orElse(null)).isNotNull();
    }
}