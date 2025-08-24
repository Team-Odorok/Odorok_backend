package com.odorok.OdorokApplication.community.controller;

import com.odorok.OdorokApplication.commons.response.CommonResponseBuilder;
import com.odorok.OdorokApplication.commons.response.ResponseRoot;
import com.odorok.OdorokApplication.community.dto.request.CommentUpdateRequest;
import com.odorok.OdorokApplication.community.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/comments")
@RequiredArgsConstructor
@RestController
public class CommentApiController {
    private final CommentService commentService;

    @Operation(summary = "게시물 댓글 수정", description = "내가 게시물에 단 댓글을 수정함")
    @ApiResponse(responseCode = "200", description = "data없음")
    @PutMapping("/{comment-id}")
    public ResponseEntity<ResponseRoot<Void>> updateComment(@PathVariable("comment-id") Long commentId,
                                                            @RequestBody CommentUpdateRequest request
    ){
        commentService.updateComment(commentId,request);
        return ResponseEntity.ok(CommonResponseBuilder.success("댓글 수정 성공"));
    }
    @Operation(summary = "게시물 댓글 삭제", description = "내가 게시물에 단 댓글을 삭제함")
    @ApiResponse(responseCode = "200", description = "data없음")
    @DeleteMapping("/{comment-id}")
    public ResponseEntity<ResponseRoot<Void>> deleteComment(@PathVariable("comment-id") Long commentId
    ){
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(CommonResponseBuilder.success("댓글 삭제 성공"));
    }
}
