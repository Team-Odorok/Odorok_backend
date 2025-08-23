package com.odorok.OdorokApplication.diary.repository;

import com.odorok.OdorokApplication.commons.querydsl.config.QueryDslConfig;
import com.odorok.OdorokApplication.course.repository.CourseRepository;
import com.odorok.OdorokApplication.domain.VisitedCourse;
import com.odorok.OdorokApplication.infrastructures.domain.Course;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
class VisitedCourseRepositoryTest {
    @Autowired
    private VisitedCourseRepository visitedCourseRepository;
    @Autowired
    private CourseRepository courseRepository;

    private final Long TEST_COURSE_ID = 1L;
    private final List<VisitedCourse> dummies = new ArrayList<>();
    private Long TARGET_COURSE_ID;
    private final Long NONTARGET_COURSE_ID = 1L;

    @BeforeEach
    public void setup() {
//        insert into courses(idx, route_idx, cycle, brd_div, created_at, modified_at) values ('test0000', 'T_ROUTE_MNG0000000001', 0, 1, now(), now());i
//        select * from courses where idx = 'test0000';
//        delete from courses where idx like 'test%';
        Course dummyCourse = Course.builder().idx("test0000").routeIdx("T_ROUTE_MNG0000000001").cycle(false).brdDiv(true).createdAt(LocalDateTime.now()).modifiedAt(LocalDateTime.now()).build();
        courseRepository.save(dummyCourse);

        TARGET_COURSE_ID = dummyCourse.getId();

        dummies.add(VisitedCourse.builder()
                .courseId(TARGET_COURSE_ID).visitedAt(LocalDateTime.now()).userId(1L).startCoordsId(1L).endCoordsId(2L).distance(3.4).stars(1).review("리뷰1").isFinished(false).build());
        dummies.add(VisitedCourse.builder()
                .courseId(TARGET_COURSE_ID).visitedAt(LocalDateTime.now()).userId(1L).startCoordsId(1L).endCoordsId(2L).distance(9.1).stars(5).isFinished(true).build());
        dummies.add(VisitedCourse.builder()
                .courseId(TARGET_COURSE_ID).visitedAt(LocalDateTime.now()).userId(1L).startCoordsId(1L).endCoordsId(2L).distance(12.2).stars(7).review("리뷰3").isFinished(false).build());
        dummies.add(VisitedCourse.builder()
                .courseId(TARGET_COURSE_ID).visitedAt(LocalDateTime.now()).userId(1L).startCoordsId(1L).endCoordsId(2L).distance(3.1).stars(9).review("리뷰4").isFinished(true).build());
        dummies.add(VisitedCourse.builder()
                .courseId(TARGET_COURSE_ID).visitedAt(LocalDateTime.now()).userId(1L).startCoordsId(1L).endCoordsId(2L).distance(5.5).stars(10).isFinished(true).build());
        dummies.add(VisitedCourse.builder()
                .courseId(NONTARGET_COURSE_ID).visitedAt(LocalDateTime.now()).userId(1L).startCoordsId(1L).endCoordsId(2L).distance(5.5).stars(0).review("리뷰6").isFinished(true).build());
        visitedCourseRepository.saveAll(dummies);
    }

    @Test
    public void 평균_별점을_읽어오는데_성공한다() {
        Long expect = Math.round(dummies.stream().filter(d -> d.getCourseId().equals(TARGET_COURSE_ID)).collect(Collectors.averagingDouble(VisitedCourse::getStars)));
        Long actual = Math.round(visitedCourseRepository.findAvgStarsByCourseId(TARGET_COURSE_ID));
        System.out.println(String.format("expect : %d, actual : %d", expect, actual));
        assertThat(actual).isEqualTo(expect);
    }

    @Test
    public void 리뷰수를_조회하는데_성공한다() {
        Long count = visitedCourseRepository.countReviewsOf(TARGET_COURSE_ID);
        assertThat(count).isEqualTo(3);
    }

    @AfterEach
    public void cleanup() {
        visitedCourseRepository.deleteAll(dummies);
        courseRepository.deleteById(TARGET_COURSE_ID);
    }
}