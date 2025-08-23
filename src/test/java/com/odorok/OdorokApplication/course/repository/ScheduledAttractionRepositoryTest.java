package com.odorok.OdorokApplication.course.repository;

import com.odorok.OdorokApplication.commons.querydsl.config.QueryDslConfig;
import com.odorok.OdorokApplication.domain.ScheduledAttraction;
import com.odorok.OdorokApplication.domain.ScheduledCourse;
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
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
class ScheduledAttractionRepositoryTest {
    @Autowired
    private ScheduledCourseRepository scheduledCourseRepository;

    @Autowired
    private ScheduledAttractionRepository scheduledAttractionRepository;

    private static final long TEST_COURSE_ID = 1L;
    private static final long TEST_USER_ID = 1L;
    private static final long TEST_ATTRACTION_ID = 1L;


    private static ScheduledCourse course;

    @BeforeEach
    public void setup() {
        course =  scheduledCourseRepository.save(new ScheduledCourse(null, TEST_COURSE_ID, LocalDateTime.now(), TEST_USER_ID));
    }

    @Test
    public void 명소_방문_일정_등록에_성공한다() {
        List<Long> attrIds = List.of(56644L, 56645L, 56646L);

        for(Long id : attrIds) {
            ScheduledAttraction attr = new ScheduledAttraction(null, id, course.getId());
            scheduledAttractionRepository.save(attr);
        }

        long count = scheduledAttractionRepository.countByScourseId(course.getId());
        assertThat(count).isEqualTo(3);
    }

    @Test
    @Transactional
    public void 방문예정_명소_조회에_성공한다() {
        List<Long> attrIds = List.of(56644L, 56645L, 56646L);
        List<ScheduledAttraction> answer = new ArrayList<>();

        for(Long id : attrIds) {
            ScheduledAttraction attr = new ScheduledAttraction(null, id, course.getId());
            answer.add(scheduledAttractionRepository.save(attr));
        }

        answer.sort(Comparator.comparing(ScheduledAttraction::getId));

        List<ScheduledAttraction> schedule = scheduledAttractionRepository.findByScourseId(course.getId());
        schedule.sort(Comparator.comparing(ScheduledAttraction::getId));

        assertThat(schedule).isEqualTo(answer);
    }

    @Test
    @Transactional
    public void 방문예정_명소_삭제에_성공한다() {
        // given
        List<Long> attrIds = List.of(56644L, 56645L, 56646L);
        List<ScheduledAttraction> answer = new ArrayList<>();

        for(Long id : attrIds) {
            ScheduledAttraction attr = new ScheduledAttraction(null, id, course.getId());
            answer.add(scheduledAttractionRepository.save(attr));
        }

        // when
        Long count = scheduledAttractionRepository.deleteByScourseId(course.getId());

        // then
        assertThat(count).isEqualTo(3L);
        assertThat(scheduledAttractionRepository.countByScourseId(course.getId())).isZero();
    }

    @AfterEach
    public void cleanup() {
        scheduledCourseRepository.delete(course);
    }
}