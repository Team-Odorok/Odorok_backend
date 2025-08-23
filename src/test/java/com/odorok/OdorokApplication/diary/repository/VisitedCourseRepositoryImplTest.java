package com.odorok.OdorokApplication.diary.repository;

import com.odorok.OdorokApplication.commons.querydsl.config.QueryDslConfig;
import com.odorok.OdorokApplication.course.dto.process.CourseStat;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
class VisitedCourseRepositoryImplTest {
    @Autowired
    private VisitedCourseRepository visitedCourseRepository;

//    @Test
////    @Sql(scripts = "/sql/test-vcourse.sql")
//    @Transactional
//    public void 코스_통계_조회에_성공한다() {
//        List<CourseStat> res = visitedCourseRepository.summarizeCourseFeedback();
//        assertThat(res).isNotNull();
//        assertThat(res.size()).isNotZero();
//        System.out.println(res);
//    }
}