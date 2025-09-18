package com.odorok.OdorokApplication.community.controller;

import com.odorok.OdorokApplication.commons.response.CommonResponseBuilder;
import com.odorok.OdorokApplication.commons.response.ResponseRoot;
import com.odorok.OdorokApplication.community.dto.request.ArticleSearchCondition;
import com.odorok.OdorokApplication.community.dto.request.ArticleRegistRequest;
import com.odorok.OdorokApplication.community.dto.request.ArticleUpdateRequest;
import com.odorok.OdorokApplication.community.dto.request.CommentRegistRequest;
import com.odorok.OdorokApplication.community.dto.response.ArticleDetail;
import com.odorok.OdorokApplication.community.dto.response.ArticleSearchResponse;
import com.odorok.OdorokApplication.community.dto.response.ArticleSummary;
import com.odorok.OdorokApplication.community.dto.response.CommentSummary;
import com.odorok.OdorokApplication.community.service.ArticleService;
import com.odorok.OdorokApplication.draftDomain.Article;
import com.odorok.OdorokApplication.draftDomain.Disease;
import com.odorok.OdorokApplication.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/api/articles")
@RequiredArgsConstructor
@RestController
@Slf4j
public class ArticleApiController {
    private final ArticleService articleService;
    @Operation(summary = "게시물 전체조회", description = "등록일순,좋아요순,조회순으로 정렬가능 pageNum은 1부터 유효,50개의 게시물 제공")
    @ApiResponse(responseCode = "200", description = "조회 성공시 요약 정보들이 전송됨")
    @GetMapping("/search")
    public ResponseEntity<ResponseRoot<ArticleSearchResponse>> searchByCondition(ArticleSearchCondition cond) {
        log.debug("Request to /api/articles/search with condition: {}", cond);
        ResponseEntity<ResponseRoot<ArticleSearchResponse>> response = ResponseEntity.ok(CommonResponseBuilder.success("요청 성공", articleService.findByCondition(cond)));
        log.debug("Response from /api/articles/search: {}", response.getBody());
        return response;
    }
    @Operation(summary = "게시물을 등록한다")
    @ApiResponse(responseCode = "200", description = "data 없음")
    @PostMapping("")
    public ResponseEntity<ResponseRoot<Void>> registArticle(@RequestPart("data") ArticleRegistRequest request,
                                                            @RequestPart("images") List<MultipartFile> images,
                                                            @AuthenticationPrincipal CustomUserDetails user) {
        log.debug("Request to /api/articles for registration with data: {}, images count: {}", request, images.size());
        long userId = user.getUserId();
        articleService.insertArticle(request, images, userId);
        ResponseEntity<ResponseRoot<Void>> response = ResponseEntity.ok(CommonResponseBuilder.success("게시물이 성공적으로 등록되었습니다."));
        log.debug("Response from /api/articles for registration: {}", response.getBody());
        return response;
    }
    @Operation(summary = "게시물 상세조회")
    @ApiResponse(responseCode = "200", description = "조회 성공시 게시물 정보가 전송됨")
    @GetMapping("/{articles-id}")
    public ResponseEntity<ResponseRoot<ArticleDetail>> searchArticleDetail(@PathVariable("articles-id") Long articleId,
                                                                           @AuthenticationPrincipal CustomUserDetails user) {
        log.debug("Request to /api/articles/{} for details", articleId);
        long userId = user.getUserId();
        ArticleDetail articleDetail = articleService.findByArticleId(articleId,userId);
        ResponseEntity<ResponseRoot<ArticleDetail>> response = ResponseEntity.ok(CommonResponseBuilder.success("게시물 조회 성공", articleDetail));
        log.debug("Response from /api/articles/{}: {}", articleId, response.getBody());
        return response;
    }
    @Operation(summary = "게시물 삭제", description = "게시물을 삭제함")
    @ApiResponse(responseCode = "200", description = "data 없음")
    @DeleteMapping("/{articles-id}")
    public ResponseEntity<ResponseRoot<Void>> deleteArticle(@PathVariable("articles-id") Long articleId) {
        log.debug("Request to /api/articles/{} for deletion", articleId);
        articleService.deleteArticle(articleId);
        ResponseEntity<ResponseRoot<Void>> response = ResponseEntity.ok(CommonResponseBuilder.success("게시물이 성공적으로 삭제되었습니다."));
        log.debug("Response from /api/articles/{} for deletion: {}", articleId, response.getBody());
        return response;
    }
    @Operation(summary = "게시물 수정", description = "게시물을 수정함")
    @ApiResponse(responseCode = "200", description = "data없음")
    @PutMapping("/{articles-id}")
    public ResponseEntity<ResponseRoot<Void>> updateArticle(@RequestPart(name = "data") ArticleUpdateRequest request,
                                                            @RequestPart(name = "images") List<MultipartFile> images,
                                                            @PathVariable("articles-id") Long articleId,
                                                            @AuthenticationPrincipal CustomUserDetails user
                                                            ) {
        log.debug("Request to /api/articles/{} for update with data: {}, images count: {}", articleId, request, images.size());
        Long userId = user.getUserId();
        articleService.updateArticle(request,images,articleId,userId);
        ResponseEntity<ResponseRoot<Void>> response = ResponseEntity.ok(CommonResponseBuilder.success("게시물이 성공적으로 수정되었습니다."));
        log.debug("Response from /api/articles/{} for update: {}", articleId, response.getBody());
        return response;
    }
    @Operation(summary = "게시물 좋아요", description = "게시물에 좋아요를 달 수 있음")
    @ApiResponse(responseCode = "200", description = "data없음")
    @PostMapping("/{articles-id}/likes")
    public ResponseEntity<ResponseRoot<Void>> updateArticleLike(@PathVariable("articles-id") Long articleId,
                                                            @AuthenticationPrincipal CustomUserDetails user
    ) {
        log.debug("Request to /api/articles/{}/likes by user {}", articleId, user.getUserId());
        Long userId = user.getUserId();
        articleService.updateLike(articleId,userId);
        ResponseEntity<ResponseRoot<Void>> response = ResponseEntity.ok(CommonResponseBuilder.success("좋아요가 등록되었습니다."));
        log.debug("Response from /api/articles/{}/likes: {}", articleId, response.getBody());
        return response;
    }
    @Operation(summary = "게시물 좋아요 취소", description = "게시믈에 단 좋아요를 취소할 수 있음")
    @ApiResponse(responseCode = "200", description = "data없음")
    @PostMapping("/{articles-id}/unlikes")
    public ResponseEntity<ResponseRoot<Void>> updateArticleUnlike(@PathVariable("articles-id") Long articleId,
                                                                @AuthenticationPrincipal CustomUserDetails user
    ) {
        log.debug("Request to /api/articles/{}/unlikes by user {}", articleId, user.getUserId());
        Long userId = user.getUserId();
        articleService.updateUnlike(articleId,userId);
        ResponseEntity<ResponseRoot<Void>> response = ResponseEntity.ok(CommonResponseBuilder.success("좋아요가 취소되었습니다."));
        log.debug("Response from /api/articles/{}/unlikes: {}", articleId, response.getBody());
        return response;
    }
    @Operation(summary = "게시물 댓글 조회", description = "게시물에 등록된 댓글을 조회함")
    @ApiResponse(responseCode = "200", description = "댓글 목록 전송됨")
    @GetMapping("/{articles-id}/comments")
    public ResponseEntity<ResponseRoot<List<CommentSummary>>> searchComments(@PathVariable("articles-id") Long articleId
    ) {
        log.debug("Request to /api/articles/{}/comments for search", articleId);
        List<CommentSummary> result = articleService.findCommentsByArticleId(articleId);
        ResponseEntity<ResponseRoot<List<CommentSummary>>> response = ResponseEntity.ok(CommonResponseBuilder.success("댓글을 성공적으로 불러왔습니다.",result));
        log.debug("Response from /api/articles/{}/comments for search: {}", articleId, response.getBody());
        return response;
    }
    @Operation(summary = "게시물 댓글 등록", description = "게시물에 댓글을 등록함")
    @ApiResponse(responseCode = "200", description = "data없음")
    @PostMapping("/{articles-id}/comments")
    public ResponseEntity<ResponseRoot<Void>> registComment(@PathVariable("articles-id") Long articleId,
                                                           @AuthenticationPrincipal CustomUserDetails user,
                                                           @RequestBody CommentRegistRequest request
    ) {
        log.debug("Request to /api/articles/{}/comments for registration with request: {}", articleId, request);
        Long userId = user.getUserId();
        articleService.registComment(articleId,request,userId);
        ResponseEntity<ResponseRoot<Void>> response = ResponseEntity.ok(CommonResponseBuilder.success("댓글 작성 성공"));
        log.debug("Response from /api/articles/{}/comments for registration: {}", articleId, response.getBody());
        return response;
    }
    @Operation(summary = "게시판에서 사용할 질병 목록 반환", description = "질병 목록을 반환함")
    @ApiResponse(responseCode = "200", description = "질병 목록 반환")
    @PostMapping("/diseases")
    public ResponseEntity<ResponseRoot<List<Disease>>> findAllDisease() {
        log.debug("Request to /api/articles/diseases");
        ResponseEntity<ResponseRoot<List<Disease>>> response = ResponseEntity.ok(CommonResponseBuilder.success("질병 목록 반환 성공",articleService.findAllDisease()));
        log.debug("Response from /api/articles/diseases: {}", response.getBody());
        return response;
    }
}
