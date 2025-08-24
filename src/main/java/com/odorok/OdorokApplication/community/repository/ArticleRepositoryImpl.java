package com.odorok.OdorokApplication.community.repository;

import com.odorok.OdorokApplication.community.dto.request.ArticleSearchCondition;
import com.odorok.OdorokApplication.community.dto.response.ArticleDetail;
import com.odorok.OdorokApplication.community.dto.response.ArticleSearchResponse;
import com.odorok.OdorokApplication.community.dto.response.ArticleSummary;
import com.odorok.OdorokApplication.community.dto.response.QArticleSummary;
import com.odorok.OdorokApplication.domain.QUser;
import com.odorok.OdorokApplication.draftDomain.*;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
@RequiredArgsConstructor
@Repository
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QArticle article = QArticle.article;
    private final QUser user = QUser.user;
    private final QTier tier = QTier.tier;
    private final QProfile profile = QProfile.profile;

    @Override
    public ArticleSearchResponse findByCondition(ArticleSearchCondition cond) {
        //pageSize는 null 이거나 음수인 경우 50으로 고정함
        int pageSize = (cond.getPageSize() == null || cond.getPageSize() <= 0) ? 50 : cond.getPageSize();
        //firstId가 null이 아니거나 lastId가 null이 아닌 경우 true 둘 다 null이라면 false
        boolean hasCursor = (cond.getFirstId() != null) || (cond.getLastId() != null);
        //diseaseId가 null이 아니라면 article.diseaseId.eq(diseaseId)라는 조건절을 제공
        BooleanExpression base = diseaseEqOrNull(cond.getDiseaseId()); // 카테고리 옵션

        List<Long> idList;
        //firstId lastId 둘 다 null <- 첫게시판 입장하거나, 정렬기준 선택하거나,카테고리를 선택했을 때
        if (!hasCursor) {
            //order의 기준은 like,view인 경우 카운트 기준 내림차순, 동일하다면 id내림차순 추가정렬
            OrderSpecifier<?>[] order = displayOrder(cond.getSort()); // like/view/createdAt 모두 DESC, id DESC 보조
            //offset은 프론트에서 첫페이지번호를 1부터 받을 수 있도록 지원함
            long offset = (long) Math.max(cond.getPageNum() - 1, 0) * pageSize;
            //쿼리문은 select id from article where diseaseId=id(있다면) order By (정렬기준에 맞게 정렬) offset limit로 50개만 가져옴
            idList = queryFactory.select(article.id)
                    .from(article)
                    .where(base)
                    .orderBy(order)
                    .offset(offset)
                    .limit(pageSize)
                    .fetch();

        } else { //firstId나 lastId가 존재하는 경우 -> 페이지를 받아온 상태에서 이동하고자 하는 경우
            //현재 위치한 페이지
            int from = cond.getCurrentPageNum();
            //이동하고자 하는 페이지
            int to = cond.getPageNum();
            //앞으로 이동하는가 뒤로 이동하는가에 대해 판단
            boolean goingBackward = to < from;                // 3→1(최신쪽) vs 3→5(오래된쪽)
            //이동하려 하는 간격
            int hop = Math.max(Math.abs(to - from) - 1, 0);   // 중간 페이지 수

            BooleanExpression range = rangeByDirection(cond, goingBackward); //조건절을 반환 더 큰 id값을 받아올지 더 작은 id값을 받아올지
            OrderSpecifier<?>[] fetchOrder = fetchOrder(cond.getSort(), goingBackward); //앞이나 뒤로 가기 위해서 올바른 정렬 기준을 받는다
            //select id from article where diseaseId=id(있다면) and (id>firstId 혹은 id<lastId) order by (likecount or viewcount) offset limit로 행 받아옴
            idList = queryFactory.select(article.id)
                    .from(article)
                    .where(andAll(base, range))
                    .orderBy(fetchOrder)
                    .offset((long) hop * pageSize)
                    .limit(pageSize)
                    .fetch();
        }

        if (idList.isEmpty()) {
            return new ArticleSearchResponse(
                    Collections.emptyList(),
                    calcEndPageInBlock(cond),
                    null,  // firstId
                    null,  // lastId
                    null,  // firstLike
                    null,  // lastLike
                    null,  // firstView
                    null   // lastView
            );
        }

        // 최종 표시 정렬은 항상 사용자가 고른 기준 DESC (+ id DESC)
        OrderSpecifier<?>[] displayOrder = displayOrder(cond.getSort());

        List<ArticleSummary> rows = queryFactory
                .select(new QArticleSummary(
                        article.id,
                        article.title,
                        article.createdAt,
                        article.likeCount,
                        article.viewCount,
                        article.commentCount,
                        article.boardType,
                        article.notice,
                        user.nickname
                ))
                .from(article)
                .join(user).on(article.userId.eq(user.id))
                .where(andAll(base, article.id.in(idList))) // 안전하게 base도 함께
                .orderBy(displayOrder)
                .fetch();

        Long firstId = rows.stream().map(ArticleSummary::getId).max(Long::compareTo).orElse(null);
        Long lastId  = rows.stream().map(ArticleSummary::getId).min(Long::compareTo).orElse(null);

        Integer firstLike = rows.stream().map(ArticleSummary::getLikeCount).max(Integer::compareTo).orElse(null);
        Integer lastLike  = rows.stream().map(ArticleSummary::getLikeCount).min(Integer::compareTo).orElse(null);

        Integer firstView = rows.stream().map(ArticleSummary::getViewCount).max(Integer::compareTo).orElse(null);
        Integer lastView  = rows.stream().map(ArticleSummary::getViewCount).min(Integer::compareTo).orElse(null);

        int endPage = calcEndPageInBlock(cond);

        return new ArticleSearchResponse(rows, endPage, firstId, lastId, firstLike, lastLike, firstView, lastView);
    }

    private BooleanExpression diseaseEqOrNull(Integer diseaseId) {
        return (diseaseId == null) ? null : article.diseaseId.eq(diseaseId);
    }

    private BooleanExpression rangeByDirection(ArticleSearchCondition cond, boolean goingBackward) {
        String sort = cond.getSort();
        switch (sort) {
            case "likeCount":
                return likeCursor(cond, goingBackward);
            case "viewCount":
                return viewCursor(cond, goingBackward);
            case "createdAt":
            default:
                return createdAtCursor(cond, goingBackward);
        }
    }

    private BooleanExpression likeCursor(ArticleSearchCondition cond, boolean goingBackward) {
        if (goingBackward) { // 앞으로 (이전 페이지)
            if (cond.getFirstLike() == null || cond.getFirstId() == null) return null;
            return article.likeCount.gt(cond.getFirstLike())
                    .or(article.likeCount.eq(cond.getFirstLike()).and(article.id.gt(cond.getFirstId())));
        } else { // 뒤로 (다음 페이지)
            if (cond.getLastLike() == null || cond.getLastId() == null) return null;
            return article.likeCount.lt(cond.getLastLike())
                    .or(article.likeCount.eq(cond.getLastLike()).and(article.id.lt(cond.getLastId())));
        }
    }

    private BooleanExpression viewCursor(ArticleSearchCondition cond, boolean goingBackward) {
        if (goingBackward) {
            if (cond.getFirstView() == null || cond.getFirstId() == null) return null;
            return article.viewCount.gt(cond.getFirstView())
                    .or(article.viewCount.eq(cond.getFirstView()).and(article.id.gt(cond.getFirstId())));
        } else {
            if (cond.getLastView() == null || cond.getLastId() == null) return null;
            return article.viewCount.lt(cond.getLastView())
                    .or(article.viewCount.eq(cond.getLastView()).and(article.id.lt(cond.getLastId())));
        }
    }

    private BooleanExpression createdAtCursor(ArticleSearchCondition cond, boolean goingBackward) {
        if (goingBackward) {
            return (cond.getFirstId() == null) ? null : article.id.gt(cond.getFirstId());
        } else {
            return (cond.getLastId() == null) ? null : article.id.lt(cond.getLastId());
        }
    }

    @Override
    public ArticleDetail findArticleDetailById(Long articleId) {
        return queryFactory.select(Projections.constructor(ArticleDetail.class,
                        article.id,
                        article.title,
                        article.content,
                        article.createdAt,
                        article.likeCount,
                        article.viewCount,
                        article.commentCount,
                        article.notice,
                        article.userId,
                        user.nickname,
                        tier.title))
                .from(article)
                .join(user).on(article.userId.eq(user.id))
                .join(profile).on(article.userId.eq(profile.userId))
                .join(tier).on(profile.tierId.eq(tier.id))
                .where(article.id.eq(articleId))
                .fetchOne();
    }

    @Nullable
    private BooleanExpression titleLike(String title) {
        return StringUtils.hasText(title) ? article.title.like("%" + title + "%") : null;
    }

    private OrderSpecifier<?>[] displayOrder(String sort) {
        switch (sort) {
            case "likeCount":
                return new OrderSpecifier<?>[]{article.likeCount.desc(), article.id.desc()};
            case "viewCount":
                return new OrderSpecifier<?>[]{article.viewCount.desc(), article.id.desc()};
            case "createdAt":
            default:
                return new OrderSpecifier<?>[]{article.id.desc()};
        }
    }

    private OrderSpecifier<?>[] fetchOrder(String sort, boolean goingBackward) {
        switch (sort) {
            case "likeCount":
                return goingBackward //앞으로 페이지를 이동하는 상황이라면 좋아요 기준으로는 오름차순으로 정렬해야 한다 이유는 좋아요수가 더 큰 행들 중에서 가장 좋아요가 작은 행들로 제공되어야 하기 때문
                        ? new OrderSpecifier<?>[]{article.likeCount.asc(), article.id.asc()}   // 3→1
                        //뒤로 페이지를 이동하는 경우라면 좋아요를 내림차순으로 할것 이유는 좋아요수가 더 작은 행들 중에서 가장 좋아요사 큰 행들로 제공되어야 하기 때문
                        : new OrderSpecifier<?>[]{article.likeCount.desc(), article.id.desc()};// 3→5
            case "viewCount":
                return goingBackward
                        //좋아요와 똑같은 이유들로 인해 각각 asc, desc
                        ? new OrderSpecifier<?>[]{article.viewCount.asc(), article.id.asc()}
                        : new OrderSpecifier<?>[]{article.viewCount.desc(), article.id.desc()};
            case "createdAt":
            default:
                return goingBackward
                        ? new OrderSpecifier<?>[]{article.id.asc()}
                        : new OrderSpecifier<?>[]{article.id.desc()};
        }
    }

    private BooleanExpression andAll(BooleanExpression... exprs) {
        BooleanExpression ret = null;
        for (BooleanExpression e : exprs) {
            if (e == null) continue;
            ret = (ret == null) ? e : ret.and(e);
        }
        return ret;
    }

    private int calcEndPageInBlock(ArticleSearchCondition cond) {
        int pageSize = (cond.getPageSize() == null || cond.getPageSize() <= 0) ? 50 : cond.getPageSize();
        int current = cond.getCurrentPageNum();
        int blockEnd = ((current + 9) / 10) * 10;
        int pagesPossible = blockEnd - current;
        if (pagesPossible <= 0) return current;

        // 3→5 조건 재사용: id < lastId (lastId 없으면 capRows=0으로 처리)
        if (cond.getLastId() == null) return current; // 최초 입장 등은 더 모를 수 있음(확실하지 않음)

        Long count = queryFactory
                .select(article.id.count())
                .from(article)
                .where(andAll(
                        diseaseEqOrNull(cond.getDiseaseId()),
                        article.id.lt(cond.getLastId())
                ))
                .fetchOne();

        long capRows = (long) pagesPossible * pageSize;
        long rowsCapped = Math.min(count == null ? 0L : count, capRows);
        int addPages = (int) Math.ceil(rowsCapped / (double) pageSize);

        return current + addPages;
    }
}

