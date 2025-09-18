package com.odorok.OdorokApplication.visitedCourse.controller;

import com.odorok.OdorokApplication.commons.response.CommonResponseBuilder;
import com.odorok.OdorokApplication.commons.response.ResponseRoot;
import com.odorok.OdorokApplication.security.dto.CustomUserDetails;
import com.odorok.OdorokApplication.visitedCourse.dto.response.VisitedCourseDetail;
import com.odorok.OdorokApplication.visitedCourse.dto.response.VisitedCourseResponseInfo;
import com.odorok.OdorokApplication.visitedCourse.dto.response.VisitedCourseSummaryWithGilName;
import com.odorok.OdorokApplication.visitedCourse.service.VisitedCourseService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/visited-courses")
@RequiredArgsConstructor
public class VisitedCourseController {

    private final VisitedCourseService visitedCourseService;

    @GetMapping
    @Operation(summary = "방문 완료한 코스 목록,후기 동시조회", description = "사용자가 방문 완료한 코스 목록을 조회하고 후기가 존재한다면 따로 보냄")
    public ResponseEntity<ResponseRoot<VisitedCourseResponseInfo>> getVisitedCourses(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        VisitedCourseResponseInfo visitedCourses = visitedCourseService.getVisitedCourses(userId);
        return ResponseEntity.ok(CommonResponseBuilder.success("방문 완료 코스 목록 조회 성공", visitedCourses));
    }

    @GetMapping("/{visited-courses_id}")
    @Operation(summary = "방문 완료한 코스 상세 정보 조회", description = "사용자가 방문 완료한 코스의 상세 정보를 조회합니다.")
    public ResponseEntity<ResponseRoot<VisitedCourseDetail>> getVisitedCourseDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("visited-courses_id") Long visitedCourseId) {
        Long userId = userDetails.getUserId();
        VisitedCourseDetail visitedCourseDetail = visitedCourseService.getVisitedCourseDetail(userId, visitedCourseId);
        return ResponseEntity.ok(CommonResponseBuilder.success("방문 완료 코스 상세 정보 조회 성공", visitedCourseDetail));
    }

    @PostMapping("/{visited-courses_id}/reviews")
    @Operation(summary = "방문 완료 코스 후기 작성/수정", description = "방문 완료한 코스에 대한 후기를 작성하거나 수정합니다.")
    public ResponseEntity<ResponseRoot<Void>> createOrUpdateReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("visited-courses_id") Long visitedCourseId,
            @RequestParam("star") int star,
            @RequestParam("review") String review,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        Long userId = userDetails.getUserId();
        visitedCourseService.createOrUpdateReview(userId, visitedCourseId, star, review, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponseBuilder.success("후기 작성 성공"));
    }
}
