package com.odorok.OdorokApplication.community.repository;

import com.odorok.OdorokApplication.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like,Long> {
    Long deleteByArticleIdAndUserId(Long articleId, Long userId);

    Optional<Like> findByArticleIdAndUserId(Long articleId, Long userId);
}
