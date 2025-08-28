package com.odorok.OdorokApplication.mypage.repository;

import com.odorok.OdorokApplication.domain.QAttendanceHistory;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class AttendanceRepositoryImpl implements AttendanceRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    @Override
    public List<Integer> findAttendedDaysByMonth(Long userId, LocalDateTime start, LocalDateTime end) {
        QAttendanceHistory a = QAttendanceHistory.attendanceHistory;

        // MySQL: DAY(attended_at)
        NumberExpression<Integer> dayExpr =
                Expressions.numberTemplate(Integer.class, "day({0})", a.attendedAt);

        return queryFactory
                .select(dayExpr)
                .from(a)
                .where(
                        a.userId.eq(userId),
                        a.attendedAt.goe(start),
                        a.attendedAt.lt(end)
                )
                .distinct()
                .orderBy(dayExpr.asc())
                .fetch();
    }
}
