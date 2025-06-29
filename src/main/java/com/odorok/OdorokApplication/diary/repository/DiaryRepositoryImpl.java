package com.odorok.OdorokApplication.diary.repository;

import com.odorok.OdorokApplication.diary.dto.response.DiaryDetail;
import com.odorok.OdorokApplication.domain.QDiary;
import com.odorok.OdorokApplication.domain.QDiaryImage;
import com.odorok.OdorokApplication.domain.QVisitedCourse;
import com.odorok.OdorokApplication.infrastructures.domain.QCourse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DiaryRepositoryImpl implements DiaryRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    QDiary diary = QDiary.diary;
    QDiaryImage diaryImage = QDiaryImage.diaryImage;
    QVisitedCourse visitedCourse = QVisitedCourse.visitedCourse;
    QCourse course = QCourse.course;

    @Override
    public DiaryDetail findDiaryById(long userId, long diaryId) {
        //diary
        DiaryDetail diaryDetail = queryFactory
                .select(Projections.constructor(
                        DiaryDetail.class,
                        diary.id,
                        diary.title,
                        diary.content,
                        diary.userId,
                        course.name,
                        visitedCourse.visitedAt,
                        diary.createdAt

                ))
                .from(diary)
                .leftJoin(visitedCourse).on(diary.vcourseId.eq(visitedCourse.id))
                .leftJoin(course).on(visitedCourse.courseId.eq(course.id))
                .where(diary.id.eq(diaryId), diary.userId.eq(userId))
                .fetchOne();

        List<String> imgs = queryFactory
                .select(diaryImage.imgUrl)
                .from(diaryImage)
                .where(diaryImage.diaryId.eq(diaryId))
                .fetch();
        diaryDetail.setImgs(imgs);
        return diaryDetail;
    }
}
