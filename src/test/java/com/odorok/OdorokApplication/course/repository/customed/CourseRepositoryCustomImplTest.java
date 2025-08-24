package com.odorok.OdorokApplication.course.repository.customed;

import com.odorok.OdorokApplication.commons.querydsl.config.QueryDslConfig;
import com.odorok.OdorokApplication.course.repository.CourseRepository;
import com.odorok.OdorokApplication.infrastructures.domain.Course;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
class CourseRepositoryCustomImplTest {
    @Autowired
    private CourseRepository courseRepository;


    @BeforeEach
    public void setup() {
        courseRepository.saveAll(List.of(
                Course.builder().brdDiv(true).name("두루길").contents("두루미가 노니는 길").idx("102233BTS").build(),
                Course.builder().brdDiv(false).name("메모길").contents("기억이 뭍은 길").idx("10321433BTS").build(),
                Course.builder().brdDiv(true).name("자릿길").contents("널찍하니 쾌적한 길").idx("102233JRS").build(),
                Course.builder().brdDiv(true).name("수랏길").contents("밥집이 많은 길").idx("102233SRS").build(),
                Course.builder().brdDiv(true).name("소복길").contents("작은 행복이 모인 길").idx("102233SBS").build()

        ));
    }

    @Test
    public void 유효한_시도코드_조회에_성공한다() {
        Set<Integer> sidos = courseRepository.findDistinctValidSidoCodes();
        assertThat(sidos).isNotEmpty();
        System.out.println(sidos);
    }
}