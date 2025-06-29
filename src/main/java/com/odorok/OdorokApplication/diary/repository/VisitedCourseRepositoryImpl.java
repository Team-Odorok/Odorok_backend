package com.odorok.OdorokApplication.diary.repository;

import com.odorok.OdorokApplication.diary.dto.gpt.VisitedAdditionalAttraction;
import com.odorok.OdorokApplication.diary.dto.gpt.VisitedCourseAndAttraction;
import com.odorok.OdorokApplication.domain.QVisitedAttraction;
import com.odorok.OdorokApplication.domain.QVisitedCourse;
import com.odorok.OdorokApplication.draftDomain.QAttraction;
import com.odorok.OdorokApplication.infrastructures.domain.QCourse;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class VisitedCourseRepositoryImpl implements VisitedCourseRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    QVisitedCourse visitedCourses = QVisitedCourse.visitedCourse;
    QCourse courses = QCourse.course;
    QVisitedAttraction visitedAttractions = QVisitedAttraction.visitedAttraction;
    QAttraction attractions = QAttraction.attraction;

    @Override
    public List<VisitedAdditionalAttraction> findVisitedAttractionByVisitedCourseId(Long userId, Long visitedCourseId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        VisitedAdditionalAttraction.class,
                        attractions.title,
                        attractions.addr1,
                        attractions.overview
                ))
                .from(visitedCourses)
                .join(visitedAttractions).on(visitedAttractions.vcourseId.eq(visitedCourseId))
                .join(attractions).on(attractions.id.eq(visitedAttractions.attractionId))
                .where(visitedCourses.id.eq(visitedCourseId),
                        visitedCourses.userId.eq(userId))
                .fetch();
    }

    @Override
    public VisitedCourseAndAttraction findCourseAndAttractionsByVisitedCourseId(Long userId, Long visitedCourseId) {
        List<VisitedAdditionalAttraction> visitedAttractionList = findVisitedAttractionByVisitedCourseId(userId, visitedCourseId);
        Tuple tuple = jpaQueryFactory
                .select(courses.name, courses.summary)
                .from(visitedCourses)
                .join(courses).on(courses.id.eq(visitedCourses.courseId))
                .where(visitedCourses.id.eq(visitedCourseId),
                        visitedCourses.userId.eq(userId))
                .fetchOne();

        if (tuple == null) return null;

        return new VisitedCourseAndAttraction(
                tuple.get(courses.name),
                tuple.get(courses.summary),
                visitedAttractionList
        );
    }
}
