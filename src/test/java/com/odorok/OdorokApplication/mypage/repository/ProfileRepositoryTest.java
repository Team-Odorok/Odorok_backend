package com.odorok.OdorokApplication.mypage.repository;

import com.odorok.OdorokApplication.commons.querydsl.config.QueryDslConfig;
import com.odorok.OdorokApplication.community.repository.ProfileRepository;
import com.odorok.OdorokApplication.course.repository.UserRepository;
import com.odorok.OdorokApplication.domain.User;
import com.odorok.OdorokApplication.draftDomain.Profile;
import com.odorok.OdorokApplication.draftDomain.Tier;
import com.odorok.OdorokApplication.mypage.dto.response.UserInfoResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@Import(QueryDslConfig.class)
@EntityScan("com.odorok.OdorokApplication")
@EnableJpaRepositories(basePackageClasses = {ProfileRepository.class, UserRepository.class,TierRepository.class})
@Transactional
class ProfileRepositoryTest {
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TierRepository tierRepository;
    private User user;
    @BeforeEach
    void setup(){
        user = userRepository.save(User.builder().name("na").nickname("ni").email("em").password("pass").role("ro").build());
        Tier tier = tierRepository.save(Tier.builder().title("초보").level(3).bound(345).build());
        profileRepository.save(Profile.builder().userId(user.getId()).activityPoint(10).tierId(tier.getId()).imgUrl("url").build());

    }

    @Test
    void 유저_프로필_조회_성공(){
        UserInfoResponse userInfoResponse = profileRepository.findProfileByUserId(user.getId());
        assertEquals(userInfoResponse.getUserName(),"na");
        assertEquals(userInfoResponse.getUserTier(),"초보");
    }
}