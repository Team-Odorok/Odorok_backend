package com.odorok.OdorokApplication.diary.repository;

import com.odorok.OdorokApplication.course.dto.process.CourseStat;
import com.odorok.OdorokApplication.course.dto.process.QCourseStat;
import com.odorok.OdorokApplication.diary.dto.gpt.VisitedAdditionalAttraction;
import com.odorok.OdorokApplication.diary.dto.gpt.VisitedCourseAndAttraction;
import com.odorok.OdorokApplication.diary.dto.response.VisitedCourseSummary;
import com.odorok.OdorokApplication.domain.QDiary;
import com.odorok.OdorokApplication.domain.QVisitedAttraction;
import com.odorok.OdorokApplication.domain.QVisitedCourse;
import com.odorok.OdorokApplication.draftDomain.QAttraction;
import com.odorok.OdorokApplication.infrastructures.domain.QCourse;
import com.odorok.OdorokApplication.infrastructures.domain.QRoute;
import com.odorok.OdorokApplication.visitedCourse.dto.response.*;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor
public class VisitedCourseRepositoryImpl implements VisitedCourseRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    QVisitedCourse visitedCourses = QVisitedCourse.visitedCourse;
    QCourse courses = QCourse.course;
    QVisitedAttraction visitedAttractions = QVisitedAttraction.visitedAttraction;
    QAttraction attractions = QAttraction.attraction;
    QDiary diary = QDiary.diary;
    QRoute route = QRoute.route;

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
                .join(visitedAttractions).on(visitedAttractions.vcourseId.eq(visitedCourses.id))
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

    @Override
    public List<VisitedCourseSummary> findVisitedCourseWithoutDiaryByUserId(Long userId) {
        List<Tuple> tuples =  jpaQueryFactory
                .select(visitedCourses.id, visitedCourses.visitedAt, courses.name)
                .from(visitedCourses)
                .join(courses).on(courses.id.eq(visitedCourses.courseId))
                .leftJoin(diary).on(diary.vcourseId.eq(visitedCourses.id))
                .where(diary.vcourseId.isNull(),
                        visitedCourses.isFinished.isTrue(),
                        visitedCourses.userId.eq(userId))
                .fetch();
        if (tuples == null) return null;
        List<VisitedCourseSummary> result = tuples.stream()
                .map(tuple -> new VisitedCourseSummary(
                        tuple.get(visitedCourses.id),
                        tuple.get(visitedCourses.visitedAt),
                        tuple.get(courses.name)
                ))
                .collect(Collectors.toList());
        return result;
    }

    @Override
    public List<CourseStat> summarizeCourseFeedback() {
        return jpaQueryFactory.select(
                new QCourseStat(visitedCourses.courseId, visitedCourses.stars.avg(), visitedCourses.review.count(), visitedCourses.id.count())
                ).from(visitedCourses).groupBy(visitedCourses.courseId).fetch();
    }

    @Override
    public List<VisitedCourseSummaryWithGilName> findVisitedCoursesByUserId(Long userId) {
        return jpaQueryFactory
                .select(new QVisitedCourseSummaryWithGilName(
                        visitedCourses.id,
                        visitedCourses.visitedAt,
                        route.name,
                        courses.name
                ))
                .from(visitedCourses)
                .join(courses).on(visitedCourses.courseId.eq(courses.id))
                .join(route).on(courses.routeIdx.eq(route.idx))
                .where(visitedCourses.userId.eq(userId).and(visitedCourses.isFinished.isTrue()))
                .orderBy(visitedCourses.visitedAt.desc())
                .fetch();
    }

    @Override
    public Optional<VisitedCourseDetail> findDetailById(Long userId, Long visitedCourseId) {
        return Optional.ofNullable(jpaQueryFactory
                .select(new QVisitedCourseDetail(
                        visitedCourses.id,
                        visitedCourses.visitedAt,
                        visitedCourses.courseId,
                        route.name,
                        courses.name,
                        visitedCourses.distance,
                        visitedCourses.stars,
                        visitedCourses.review,
                        visitedCourses.reviewImgUrl
                ))
                .from(visitedCourses)
                .join(courses).on(visitedCourses.courseId.eq(courses.id))
                .join(route).on(courses.routeIdx.eq(route.idx))
                .where(visitedCourses.id.eq(visitedCourseId).and(visitedCourses.userId.eq(userId)))
                .fetchOne());
    }
}
