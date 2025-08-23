package com.odorok.OdorokApplication.course.repository;

import com.odorok.OdorokApplication.commons.querydsl.config.QueryDslConfig;
import com.odorok.OdorokApplication.domain.ScheduledCourse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
class ScheduledCourseRepositoryTest {
    @Autowired
    private ScheduledCourseRepository scheduledCourseRepository;

    private static final long TEST_COURSE_ID = 1L;
    private static final long TEST_USER_ID = 1L;

    @BeforeEach
    public void setup() {
        scheduledCourseRepository.save(new ScheduledCourse(null, TEST_COURSE_ID, LocalDateTime.now(), TEST_USER_ID));
        scheduledCourseRepository.save(new ScheduledCourse(null, TEST_COURSE_ID + 1, LocalDateTime.now(), TEST_USER_ID));
        scheduledCourseRepository.save(new ScheduledCourse(null, TEST_COURSE_ID + 2, LocalDateTime.now(), TEST_USER_ID));
    }

    @Test
    public void 코스_방문_예정_조회에_성공한다() {
        List<ScheduledCourse> schedule = scheduledCourseRepository.findByUserId(TEST_USER_ID);

        assertThat(schedule.size()).isNotZero();
    }
}