package com.odorok.OdorokApplication.community.repository;

import com.odorok.OdorokApplication.draftDomain.Article;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article,Long>,ArticleRepositoryCustom {
    default Article getById(Long articleId){
        return findById(articleId).orElseThrow(()-> new EntityNotFoundException("게시물이 존재하지 않습니다"));
    }
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update articles a
            set a.likeCount = a.likeCount-1
            where a.id = :articleId and a.likeCount > 0
            """)
    int decrementLikeCount(@Param("articleId") Long articleId);

   @Query("""
           select a.likeCount
           from articles a
           where a.userId = :userId
           """)
    List<Integer> findAllLikeByUserId(@Param("userId")Long id);
}
