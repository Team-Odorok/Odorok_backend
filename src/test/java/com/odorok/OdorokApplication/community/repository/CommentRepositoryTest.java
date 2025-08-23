package com.odorok.OdorokApplication.community.repository;

import com.odorok.OdorokApplication.commons.querydsl.config.QueryDslConfig;
import com.odorok.OdorokApplication.community.dto.response.CommentSummary;
import com.odorok.OdorokApplication.course.repository.UserRepository;
import com.odorok.OdorokApplication.domain.Comment;
import com.odorok.OdorokApplication.domain.User;
import com.odorok.OdorokApplication.draftDomain.Article;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
@Import(QueryDslConfig.class)
@EntityScan(basePackageClasses = {Comment.class,Article.class})
@EnableJpaRepositories(basePackageClasses = {CommentRepository.class,ArticleRepository.class})
@Transactional
@ActiveProfiles("test")
class CommentRepositoryTest {
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    ArticleRepository articleRepository;
    private Article article;
    @BeforeEach
    void setup(){
        article = articleRepository.save(new Article("첫 번째 게시글", 5, 100, 3, 0, false, 1L));
        commentRepository.save(Comment.builder().content("hi").articleId(article.getId()).userId(1L).build());
    }
    @Test
    void 댓글_조회_정상작동(){
        String expect = "hi";
        List<CommentSummary> comments = commentRepository.findCommentsByArticleId(article.getId());
        assertEquals(comments.get(0).getContent(),expect);
    }
}