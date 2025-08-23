package com.odorok.OdorokApplication.course.repository;

import com.odorok.OdorokApplication.commons.querydsl.config.QueryDslConfig;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.annotation.Immutable;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

//@DataJpaTest
//@Import(QueryDslConfig.class)
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//class DiseaseCourseStatRepositoryTest {
//    @Autowired
//    private DiseaseCourseStatRepository diseaseCourseStatRepository;
//
//    @Test
//    @Transactional
//    @Sql("/sql/disease_course_test.sql")
//    public void 질병_코스_통계_조회에_성공한다() {
//        List<DiseaseCourseStat> result = diseaseCourseStatRepository.findByKeyDiseaseId(1L);
//        result.sort((a,b) -> b.getAvgStars().compareTo(a.getAvgStars()));
//        System.out.println(result);
//        assertThat(result.size()).isNotZero();
//    }
//}