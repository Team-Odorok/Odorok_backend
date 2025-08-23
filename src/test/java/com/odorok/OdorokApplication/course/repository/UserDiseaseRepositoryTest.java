package com.odorok.OdorokApplication.course.repository;

import com.odorok.OdorokApplication.commons.querydsl.config.QueryDslConfig;
import com.odorok.OdorokApplication.domain.UserDisease;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
class UserDiseaseRepositoryTest {
    @Autowired
    private UserDiseaseRepository userDiseaseRepository;

    @Test
    @Transactional
//    @Sql("/sql/disease_course_test.sql")
    public void 질병_목록_조회에_성공한다() {
        List<UserDisease> res = userDiseaseRepository.findByUserId(1L);
//        assertThat(res.size()).isNotZero();
    }
}