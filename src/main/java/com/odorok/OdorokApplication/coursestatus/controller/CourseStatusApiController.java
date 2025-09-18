package com.odorok.OdorokApplication.coursestatus.controller;

import com.odorok.OdorokApplication.commons.response.CommonResponseBuilder;
import com.odorok.OdorokApplication.commons.response.ResponseRoot;
import com.odorok.OdorokApplication.community.dto.request.ArticleSearchCondition;
import com.odorok.OdorokApplication.community.dto.response.ArticleSearchResponse;
import com.odorok.OdorokApplication.coursestatus.dto.request.updateCourseStatusRequest;
import com.odorok.OdorokApplication.coursestatus.service.CourseStatusService;
import com.odorok.OdorokApplication.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/course")
@Slf4j
public class CourseStatusApiController {
    private final CourseStatusService courseStatusService;
    //방문 예정 코스를 방문시작하는 것
    //코스를 시작하면 현재 코스를 방문코스로 올리고 값들을 입력해줘야함(시작gpsid = 1,방문일,코스id,등록회원id)
    @Operation(summary = "코스 시작", description = "코스 여정을 시작한다.")
    @ApiResponse(responseCode = "200", description = "반환 데이터 따로 없음")
    @PostMapping("/{course-id}/start")
    public ResponseEntity<ResponseRoot<Void>> registCourseStatus(@AuthenticationPrincipal CustomUserDetails user,
                                                                @PathVariable("course-id") Long courseId) {
        log.debug("Request to /api/course/{}/start by user {}", courseId, user.getUserId());
        courseStatusService.registCourseStatus(user.getUserId(),courseId);
        ResponseEntity<ResponseRoot<Void>> response = ResponseEntity.ok(CommonResponseBuilder.success("요청 성공"));
        log.debug("Response from /api/course/{}/start: {}", courseId, response.getBody());
        return response;
    }

    //진행거리는 service에서 사용하면 됨(테스트 필요)
    @Operation(summary = "진행 거리 확인", description = "현재까지의 진행거리를 m단위로 반환")
    @ApiResponse(responseCode = "200", description = "진행 거리 반환")
    @GetMapping("/{course-id}/distance")
    public ResponseEntity<ResponseRoot<Long>> findProgress(@AuthenticationPrincipal CustomUserDetails user,
                                                       @PathVariable("course-id") Long courseId,
                                                       @RequestParam Double latitude,
                                                       @RequestParam Double longitude) {
        log.debug("Request to /api/course/{}/distance with latitude: {}, longitude: {}", courseId, latitude, longitude);
        Long distance = courseStatusService.findProgress(user.getUserId(),courseId,latitude,longitude);
        ResponseEntity<ResponseRoot<Long>> response = ResponseEntity.ok(CommonResponseBuilder.success("요청 성공",distance));
        log.debug("Response from /api/course/{}/distance: {}", courseId, response.getBody());
        return response;
    }

    //코스가 종료된다면
    //종료 gps를 넣고 체험거리를 적어주고 완료여부까지 업데이트해주면 됨
    @Operation(summary = "코스 종료", description = "코스 진행을 종료함")
    @ApiResponse(responseCode = "200", description = "반환 데이터 따로 없음")
    @PostMapping("/{course-id}/end")
    public ResponseEntity<ResponseRoot<Void>> updateCourseStatus(@AuthenticationPrincipal CustomUserDetails user,
                                                                 @PathVariable("course-id") Long courseId,
                                                                 @RequestBody updateCourseStatusRequest request) {
        log.debug("Request to /api/course/{}/end with request: {}", courseId, request);
        courseStatusService.updateCourseStatus(user.getUserId(),courseId, request.getLatitude(), request.getLongitude());
        ResponseEntity<ResponseRoot<Void>> response = ResponseEntity.ok(CommonResponseBuilder.success("요청 성공"));
        log.debug("Response from /api/course/{}/end: {}", courseId, response.getBody());
        return response;
    }
}
