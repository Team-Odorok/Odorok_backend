package com.odorok.OdorokApplication.course.repository;

import com.odorok.OdorokApplication.commons.querydsl.config.QueryDslConfig;
import com.odorok.OdorokApplication.infrastructures.domain.Course;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @BeforeEach
    public void setup() {
        courseRepository.saveAll(List.of(
                Course.builder().brdDiv(true).name("두루길").contents("두루미가 노니는 길").idx("102233BTS").build(),
                Course.builder().brdDiv(false).name("메모길").contents("기억이 뭍은 길").idx("10321433MMS").build(),
                Course.builder().brdDiv(true).name("자릿길").contents("널찍하니 쾌적한 길").idx("102233JRS").build(),
                Course.builder().brdDiv(true).name("수랏길").contents("밥집이 많은 길").idx("102233SRS").build(),
                Course.builder().brdDiv(true).name("소복길").contents("작은 행복이 모인 길").idx("102233SBS").build(),
                Course.builder().brdDiv(true).name("오솔길").contents("다람쥐가 놀다가는 길").idx("102233OSS").build(),
                Course.builder().brdDiv(false).name("몬순길").contents("강하고 따스한 바람이 부는 길").idx("104322MSS").build(),
                Course.builder().brdDiv(true).name("안갯길").contents("비오는 날 아련해지는 길").idx("102791MTS").build(),
                Course.builder().brdDiv(true).name("아랫길").contents("조금 돌아가는 길").idx("10432167990DWS").build(),
                Course.builder().brdDiv(true).name("Sesame street").contents("알록달록한 길").idx("1022543SES").build()

        ));
    }

    @Test
    public void 지역_코드로_코스_조회에_성공한다() {
        int sidoCode = 1, sigunguCode = 20;
        Assertions.assertThat(courseRepository.findBySidoCodeAndSigunguCode(sidoCode, sigunguCode, Pageable.ofSize(10)).getContent().size()).isLessThanOrEqualTo(10);
    }

    @Test
    public void 페이지_범위_바깥_조회에_성공한다() {
        Pageable pageable = PageRequest.of(10000, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Course> page = courseRepository.findAll(pageable);
        System.out.println(page.getTotalPages());
        assertThat(page.getContent().size()).isZero();
    }

    @Test
    public void 전체_코스_조회에_성공한다() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Course> page = courseRepository.findAll(pageable);
        System.out.println(page.getTotalPages());
        assertThat(page.getContent().size()).isNotZero();
    }
}