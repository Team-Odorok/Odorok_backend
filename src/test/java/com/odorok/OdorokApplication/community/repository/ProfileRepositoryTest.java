package com.odorok.OdorokApplication.community.repository;

import com.odorok.OdorokApplication.commons.querydsl.config.QueryDslConfig;
import com.odorok.OdorokApplication.draftDomain.Profile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
public class ProfileRepositoryTest {
    @Autowired
    private ProfileRepository profileRepository;

    private static final Long TEST_USER_ID = 1L;
    private static Profile dummy;

    @BeforeEach
    void setup() {
        dummy =  Profile.builder().userId(TEST_USER_ID).sidoCode(1).sidoCode(2).activityPoint(0).mileage(0).tierId(1L).build();
        profileRepository.save(dummy);
    }

    @Test
    void 유저_프로필_find에_성공한다() {
        Profile profile = profileRepository.findByUserId(TEST_USER_ID).orElseThrow(() -> new IllegalArgumentException("프로필이 존재하지 않는 아이디 입니다. (ID : " + TEST_USER_ID+")"));
        assertThat(profile).isNotNull();
        assertThat(profile.getUserId()).isEqualTo(TEST_USER_ID);
    }

    @AfterEach
    void cleanup() {
        profileRepository.delete(dummy);
    }
}