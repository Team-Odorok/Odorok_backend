package com.odorok.OdorokApplication.commons.aop.aspect;

import com.odorok.OdorokApplication.commons.aop.annotation.CheckArticleOwner;
import com.odorok.OdorokApplication.commons.aop.annotation.CheckCommentOwner;
import com.odorok.OdorokApplication.commons.auth.AuthenticationFacade;
import com.odorok.OdorokApplication.community.repository.ArticleRepository;
import com.odorok.OdorokApplication.community.repository.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class OwnershipAspect {
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final AuthenticationFacade authenticationFacade;

    @Before("@annotation(checkArticleOwner)")
    public void checkArticleOwner(JoinPoint joinPoint, CheckArticleOwner checkArticleOwner){
        //애너테이션에서 받은 "articleId"
        String paramName = checkArticleOwner.articleId();
        //메서드 시그니처(변수,변수값 추출용)
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //메서드 변수 이름
        String[] paramNames = signature.getParameterNames();
        System.out.println(Arrays.toString(signature.getParameterNames()));
        //메서드 변수 값
        Object[] args = joinPoint.getArgs();

        Long articleId = null;
        //articleId에 해당하는 변수를 받는다
        for(int i = 0; i<paramNames.length; i++){
            if(paramNames[i].equals(paramName)){
                articleId = (Long) args[i];
                break;
            }
        }
        //articleId null인 경우
        if(articleId==null){
            throw new IllegalArgumentException("articleId 존재하지 않음");
        }

        Long currentUserId = authenticationFacade.getCurrentUserId();
        Long articleOwnerId = articleRepository.getById(articleId).getUserId();
        //게시글을 수정하려는 사용자와 게시글을 작성한 사용자가 일치하지 않는 경우
        if(!currentUserId.equals(articleOwnerId)){
            throw new AccessDeniedException("해당 게시글의 소유자가 아닙니다");
        }
    }

    @Before("@annotation(checkCommentOwner)")
    public void checkCommentOwner(JoinPoint joinPoint, CheckCommentOwner checkCommentOwner){
        String paramName = checkCommentOwner.commentId();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = methodSignature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        Long commentId = null;
        for(int i = 0; i< paramNames.length; i++){
            if(paramName.equals(paramNames[i])){
                commentId = (Long) args[i];
                break;
            }
        }

        if(commentId ==null){
            throw new IllegalArgumentException("commentId 존재하지 않음");
        }

        Long currentUserId = authenticationFacade.getCurrentUserId();
        Long commentOwnerId = commentRepository.findById(commentId)
                .orElseThrow(()->new EntityNotFoundException("댓글이 존재하지 않습니다"))
                .getUserId();
        if(!currentUserId.equals(commentOwnerId)){
            throw new AccessDeniedException("해당 댓글의 소유자가 아닙니다.");
        }

    }
}
