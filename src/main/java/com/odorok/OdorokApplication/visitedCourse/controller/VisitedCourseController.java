package com.odorok.OdorokApplication.visitedCourse.controller;

import com.odorok.OdorokApplication.commons.response.CommonResponseBuilder;
import com.odorok.OdorokApplication.commons.response.ResponseRoot;
import com.odorok.OdorokApplication.security.dto.CustomUserDetails;
import com.odorok.OdorokApplication.visitedCourse.dto.response.VisitedCourseSummaryWithGilName;
import com.odorok.OdorokApplication.visitedCourse.service.VisitedCourseService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/visited-courses")
@RequiredArgsConstructor
public class VisitedCourseController {

    private final VisitedCourseService visitedCourseService;

    @GetMapping
    @Operation(summary = "방문 완료한 코스 목록 조회", description = "사용자가 방문 완료한 코스 목록을 조회합니다.")
    public ResponseEntity<ResponseRoot<List<VisitedCourseSummaryWithGilName>>> getVisitedCourses(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<VisitedCourseSummaryWithGilName> visitedCourses = visitedCourseService.getVisitedCourses(userDetails);
        return ResponseEntity.ok(CommonResponseBuilder.success("방문 완료 코스 목록 조회 성공", visitedCourses));
    }
}
