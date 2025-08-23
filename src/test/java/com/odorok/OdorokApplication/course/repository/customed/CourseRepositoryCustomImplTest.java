package com.odorok.OdorokApplication.course.repository.customed;

import com.odorok.OdorokApplication.commons.querydsl.config.QueryDslConfig;
import com.odorok.OdorokApplication.course.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
class CourseRepositoryCustomImplTest {
    @Autowired
    private CourseRepository courseRepository;

    @Test
    public void 유효한_시도코드_조회에_성공한다() {
        Set<Integer> sidos = courseRepository.findDistinctValidSidoCodes();
        assertThat(sidos).isNotEmpty();
        System.out.println(sidos);
    }
}