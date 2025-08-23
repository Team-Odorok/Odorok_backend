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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/api/articles")
@RequiredArgsConstructor
@RestController
public class ArticleApiController {
    private final ArticleService articleService;
    @Operation(summary = "게시물 전체조회", description = "등록일순,좋아요순,조회순으로 정렬가능 pageNum은 1부터 유효,50개의 게시물 제공")
    @ApiResponse(responseCode = "200", description = "조회 성공시 요약 정보들이 전송됨")
    @GetMapping("/search")
    public ResponseEntity<ResponseRoot<ArticleSearchResponse>> searchByCondition(ArticleSearchCondition cond) {
        return ResponseEntity.ok(CommonResponseBuilder.success("요청 성공", articleService.findByCondition(cond)));
    }
    @Operation(summary = "게시물을 등록한다")
    @ApiResponse(responseCode = "200", description = "data 없음")
    @PostMapping("")
    public ResponseEntity<ResponseRoot<Void>> registArticle(@RequestPart("data") ArticleRegistRequest request,
                                                            @RequestPart("images") List<MultipartFile> images,
                                                            @AuthenticationPrincipal CustomUserDetails user) {
        long userId = user.getUserId();
        articleService.insertArticle(request, images, userId);
        return ResponseEntity.ok(CommonResponseBuilder.success("게시물이 성공적으로 등록되었습니다."));
    }
    @Operation(summary = "게시물 상세조회")
    @ApiResponse(responseCode = "200", description = "조회 성공시 게시물 정보가 전송됨")
    @GetMapping("/{articles-id}")
    public ResponseEntity<ResponseRoot<ArticleDetail>> searchArticleDetail(@PathVariable("articles-id") Long articleId) {
        ArticleDetail articleDetail = articleService.findByArticleId(articleId);
        return ResponseEntity.ok(CommonResponseBuilder.success("게시물 조회 성공", articleDetail));
    }
    @Operation(summary = "게시물 삭제", description = "게시물을 삭제함")
    @ApiResponse(responseCode = "200", description = "data 없음")
    @DeleteMapping("/{articles-id}")
    public ResponseEntity<ResponseRoot<Void>> deleteArticle(@PathVariable("articles-id") Long articleId) {
        articleService.deleteArticle(articleId);
        return ResponseEntity.ok(CommonResponseBuilder.success("게시물이 성공적으로 삭제되었습니다."));
    }
    @Operation(summary = "게시물 수정", description = "게시물을 수정함")
    @ApiResponse(responseCode = "200", description = "data없음")
    @PutMapping("/{articles-id}")
    public ResponseEntity<ResponseRoot<Void>> updateArticle(@RequestPart(name = "data") ArticleUpdateRequest request,
                                                            @RequestPart(name = "images") List<MultipartFile> images,
                                                            @PathVariable("articles-id") Long articleId,
                                                            @AuthenticationPrincipal CustomUserDetails user
                                                            ) {
        Long userId = user.getUserId();
        articleService.updateArticle(request,images,articleId,userId);
        return ResponseEntity.ok(CommonResponseBuilder.success("게시물이 성공적으로 수정되었습니다."));
    }
    @Operation(summary = "게시물 좋아요", description = "게시물에 좋아요를 달 수 있음")
    @ApiResponse(responseCode = "200", description = "data없음")
    @PostMapping("/{articles-id}/likes")
    public ResponseEntity<ResponseRoot<Void>> updateArticleLike(@PathVariable("articles-id") Long articleId,
                                                            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getUserId();
        articleService.updateLike(articleId,userId);
        return ResponseEntity.ok(CommonResponseBuilder.success("좋아요가 등록되었습니다."));
    }
    @Operation(summary = "게시물 댓글 조회", description = "게시물에 등록된 댓글을 조회함")
    @ApiResponse(responseCode = "200", description = "댓글 목록 전송됨")
    @GetMapping("/{articles-id}/comments")
    public ResponseEntity<ResponseRoot<List<CommentSummary>>> searchComments(@PathVariable("articles-id") Long articleId
    ) {
        List<CommentSummary> result = articleService.findCommentsByArticleId(articleId);
        return ResponseEntity.ok(CommonResponseBuilder.success("댓글을 성공적으로 불러왔습니다.",result));
    }
    @Operation(summary = "게시물 댓글 등록", description = "게시물에 댓글을 등록함")
    @ApiResponse(responseCode = "200", description = "data없음")
    @PostMapping("/{articles-id}/comments")
    public ResponseEntity<ResponseRoot<Void>> registComment(@PathVariable("articles-id") Long articleId,
                                                           @AuthenticationPrincipal CustomUserDetails user,
                                                           @RequestBody CommentRegistRequest request
    ) {
        Long userId = user.getUserId();
        articleService.registComment(articleId,request,userId);
        return ResponseEntity.ok(CommonResponseBuilder.success("댓글 작성 성공"));
    }
    @Operation(summary = "게시판에서 사용할 질병 목록 반환", description = "질병 목록을 반환함")
    @ApiResponse(responseCode = "200", description = "질병 목록 반환")
    @PostMapping("/diseases")
    public ResponseEntity<ResponseRoot<List<Disease>>> findAllDisease() {
        return ResponseEntity.ok(CommonResponseBuilder.success("질병 목록 반환 성공",articleService.findAllDisease()));
    }
}
