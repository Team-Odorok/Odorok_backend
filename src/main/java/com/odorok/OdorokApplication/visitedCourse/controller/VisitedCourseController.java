package com.odorok.OdorokApplication.visitedCourse.controller;

import com.odorok.OdorokApplication.commons.response.CommonResponseBuilder;
import com.odorok.OdorokApplication.commons.response.ResponseRoot;
import com.odorok.OdorokApplication.security.dto.CustomUserDetails;
import com.odorok.OdorokApplication.visitedCourse.dto.response.VisitedCourseDetail;
import com.odorok.OdorokApplication.visitedCourse.dto.response.VisitedCourseSummaryWithGilName;
import com.odorok.OdorokApplication.visitedCourse.service.VisitedCourseService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/visited-courses")
@RequiredArgsConstructor
@Slf4j
public class VisitedCourseController {

    private final VisitedCourseService visitedCourseService;

    @GetMapping
    @Operation(summary = "방문 완료한 코스 목록 조회", description = "사용자가 방문 완료한 코스 목록을 조회합니다.")
    public ResponseEntity<ResponseRoot<List<VisitedCourseSummaryWithGilName>>> getVisitedCourses(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.debug("Request to /api/visited-courses for user: {}", userDetails.getUserId());
        Long userId = userDetails.getUserId();
        List<VisitedCourseSummaryWithGilName> visitedCourses = visitedCourseService.getVisitedCourses(userId);
        ResponseEntity<ResponseRoot<List<VisitedCourseSummaryWithGilName>>> response = ResponseEntity.ok(CommonResponseBuilder.success("방문 완료 코스 목록 조회 성공", visitedCourses));
        log.debug("Response from /api/visited-courses: {}", response.getBody());
        return response;
    }

    @GetMapping("/{visited-courses_id}")
    @Operation(summary = "방문 완료한 코스 상세 정보 조회", description = "사용자가 방문 완료한 코스의 상세 정보를 조회합니다.")
    public ResponseEntity<ResponseRoot<VisitedCourseDetail>> getVisitedCourseDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("visited-courses_id") Long visitedCourseId) {
        log.debug("Request to /api/visited-courses/{} for details", visitedCourseId);
        Long userId = userDetails.getUserId();
        VisitedCourseDetail visitedCourseDetail = visitedCourseService.getVisitedCourseDetail(userId, visitedCourseId);
        ResponseEntity<ResponseRoot<VisitedCourseDetail>> response = ResponseEntity.ok(CommonResponseBuilder.success("방문 완료 코스 상세 정보 조회 성공", visitedCourseDetail));
        log.debug("Response from /api/visited-courses/{}: {}", visitedCourseId, response.getBody());
        return response;
    }

    @PostMapping("/{visited-courses_id}/reviews")
    @Operation(summary = "방문 완료 코스 후기 작성/수정", description = "방문 완료한 코스에 대한 후기를 작성하거나 수정합니다.")
    public ResponseEntity<ResponseRoot<Void>> createOrUpdateReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("visited-courses_id") Long visitedCourseId,
            @RequestPart("star") int star,
            @RequestPart("review") String review,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        log.debug("Request to /api/visited-courses/{}/reviews for review creation/update with star: {}, review: {}, image present: {}", visitedCourseId, star, review, image != null && !image.isEmpty());
        Long userId = userDetails.getUserId();
        visitedCourseService.createOrUpdateReview(userId, visitedCourseId, star, review, image);
        ResponseEntity<ResponseRoot<Void>> response = ResponseEntity.status(HttpStatus.CREATED).body(CommonResponseBuilder.success("후기 작성 성공"));
        log.debug("Response from /api/visited-courses/{}/reviews: {}", visitedCourseId, response.getBody());
        return response;
    }
}
