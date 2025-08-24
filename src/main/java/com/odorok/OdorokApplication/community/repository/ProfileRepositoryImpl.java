package com.odorok.OdorokApplication.community.repository;


import com.odorok.OdorokApplication.domain.QUser;
import com.odorok.OdorokApplication.draftDomain.QProfile;
import com.odorok.OdorokApplication.draftDomain.QTier;
import com.odorok.OdorokApplication.mypage.dto.response.UserInfoResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProfileRepositoryImpl implements ProfileRepositoryCustom{
    private final JPAQueryFactory queryFactory;
    private final QUser user = QUser.user;
    private final QProfile profile = QProfile.profile;
    private final QTier tier = QTier.tier;

    @Override
    public UserInfoResponse findProfileByUserId(Long userId) {

        UserInfoResponse userInfo = queryFactory.select(Projections.constructor(UserInfoResponse.class,
                profile.imgUrl,user.name,tier.level,tier.title,profile.activityPoint))
                .from(user)
                .join(profile).on(user.id.eq(profile.userId))
                .join(tier).on(tier.id.eq(profile.tierId))
                .where(user.id.eq(userId))
                .fetchOne();

        return userInfo;
    }
}
