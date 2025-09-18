package com.odorok.OdorokApplication.course.controller;

import com.odorok.OdorokApplication.commons.response.CommonResponseBuilder;
import com.odorok.OdorokApplication.commons.response.ResponseRoot;
import com.odorok.OdorokApplication.course.dto.request.CourseScheduleRequest;
import com.odorok.OdorokApplication.course.dto.response.holder.CourseResponse;
import com.odorok.OdorokApplication.course.dto.response.holder.DiseaseCourseResponse;
import com.odorok.OdorokApplication.course.dto.response.holder.TopRatedCourseResponse;
import com.odorok.OdorokApplication.course.dto.response.holder.VisitationScheduleResponse;
import com.odorok.OdorokApplication.course.dto.response.item.CourseDetail;
import com.odorok.OdorokApplication.course.dto.response.item.CourseSummary;
import com.odorok.OdorokApplication.course.dto.response.item.DiseaseAndCourses;
import com.odorok.OdorokApplication.course.dto.response.item.VisitationScheduleSummary;
import com.odorok.OdorokApplication.course.service.CourseQueryService;
import com.odorok.OdorokApplication.course.service.CourseScheduleManageService;
import com.odorok.OdorokApplication.course.service.CourseScheduleQueryService;
import com.odorok.OdorokApplication.course.service.ProfileQueryService;
import com.odorok.OdorokApplication.domain.User;
import com.odorok.OdorokApplication.draftDomain.Profile;
import com.odorok.OdorokApplication.region.exception.InvalidSidoCodeException;
import com.odorok.OdorokApplication.security.dto.CustomUserDetails;
import com.odorok.OdorokApplication.security.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses")
@Slf4j
@Tag(name = "코스 관련 API")
public class CourseApiController {
    private final CourseQueryService courseQueryService;
    private final UserService userService;
    private final ProfileQueryService profileQueryService;
    private final CourseScheduleManageService courseScheduleManageService;
    private final CourseScheduleQueryService courseScheduleQueryService;

    @GetMapping("/region")
    @Operation(summary = "지역 코드로 코스를 검색한다.", description = "시도 코드, 시군구 코드를 기반으로 검색합니다. 정렬 기준은 지정가능합니다. 지정하지 않으면, 생성일이 기준입니다. 기본 페이지 사이즈는 10이며 0번 페이지부터 시작합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공시 요약 정보들이 전송됨")
    @ApiResponse(responseCode = "400", description = "시도 코드가 유효하지 않으면 400 에러가 출력됨.")
    @ApiResponse(responseCode = "500", description = "서버 내부에서 조회에 실패하는 경우임.")
    public ResponseEntity<ResponseRoot<CourseResponse>> searchByRegionCode(@RequestParam("sidoCode") Integer sidoCode,
                                                                           @RequestParam("sigunguCode") Integer sigunguCode,
                                                                           @AuthenticationPrincipal CustomUserDetails user,
                                                                           @PageableDefault(size = 10, page = 0, sort = "createdAt") Pageable pageable) {
        log.debug("Request to /api/courses/region with sidoCode: {}, sigunguCode: {}, pageable: {}", sidoCode, sigunguCode, pageable);
        String email = (user != null ? user.getUsername() : null);

        if(!courseQueryService.checkSidoCodeValidation(sidoCode)) {
            ResponseEntity<ResponseRoot<CourseResponse>> response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonResponseBuilder.fail("유효하지 않은 시도 코드 입니다. " + sidoCode));
            log.debug("Error response from /api/courses/region: {}", response.getBody());
            return response;
        }

        Long userId = null;
        if (email != null) userId = userService.queryByEmail(email).getId();
        CourseResponse courseResponse = new CourseResponse();
        try {
            courseResponse.setItems(courseQueryService.queryCoursesByRegion(sidoCode, sigunguCode, userId, pageable));
        } catch (RuntimeException e) {
            log.debug(e.getMessage());
            ResponseEntity<ResponseRoot<CourseResponse>> response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonResponseBuilder.fail("지역 코스 검색에 실패했습니다."));
            log.debug("Error response from /api/courses/region: {}", response.getBody());
            return response;
        }
        ResponseEntity<ResponseRoot<CourseResponse>> response = ResponseEntity.status(HttpStatus.OK).body(CommonResponseBuilder.success("", courseResponse));
        log.debug("Response from /api/courses/region: {}", response.getBody());
        return response;
    }

    // 전체 코스 리스트
    @GetMapping("")
    @Operation(summary = "전체 코스 목록에서 조회한다.", description = "정렬 기준은 지정가능합니다. 지정하지 않으면, 생성일이 기준입니다. 기본 페이지 사이즈는 10이며 0번 페이지부터 시작합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공시 요약 정보들이 전송됨")
    @ApiResponse(responseCode = "500", description = "서버 내부에서 조회에 실패하는 경우임.")
    public ResponseEntity<ResponseRoot<CourseResponse>> getAllCourses(
            @AuthenticationPrincipal CustomUserDetails user,
            @PageableDefault(size = 10, page = 0, sort = "createdAt") Pageable pageable) {
        log.debug("Request to /api/courses with pageable: {}", pageable);
        String email = (user != null) ? user.getUsername() : null;
        Long userId = null;
        if (email != null) userId = userService.queryByEmail(email).getId();
        try {
            List<CourseSummary> result = courseQueryService.queryAllCourses(userId, pageable);
            ResponseEntity<ResponseRoot<CourseResponse>> response = ResponseEntity.status(HttpStatus.OK).body(CommonResponseBuilder.success("", new CourseResponse(result)));
            log.debug("Response from /api/courses: {}", response.getBody());
            return response;
        } catch (RuntimeException e) {
            log.debug(e.getMessage());
            ResponseEntity<ResponseRoot<CourseResponse>> response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonResponseBuilder.fail("전체 코스 조회에 실패했습니다."));
            log.debug("Error response from /api/courses: {}", response.getBody());
            return response;
        }
    }

    // 코스 상세 조회
    @GetMapping("/detail")
    @Operation(summary = "코스의 상세 정보를 조회한다.", description = "어떤 코스에 대해서 가지고 있는 세부 정보 조회를 합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공시 세부 정보들이 전송됨")
    @ApiResponse(responseCode = "400", description = "존재하지 않는 코스 ID로 조회하는 경우임.")
    public ResponseEntity<ResponseRoot<CourseDetail>> getCourseDetail(
            @RequestParam("courseId") Long courseId) {
        log.debug("Request to /api/courses/detail with courseId: {}", courseId);
        try {
            ResponseEntity<ResponseRoot<CourseDetail>> response = ResponseEntity.status(HttpStatus.OK)
                    .body(CommonResponseBuilder.success("", courseQueryService.queryCourseDetail(courseId)));
            log.debug("Response from /api/courses/detail: {}", response.getBody());
            return response;
        } catch (IllegalArgumentException e) {
            log.debug(e.getMessage());
            ResponseEntity<ResponseRoot<CourseDetail>> response = ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(CommonResponseBuilder.fail("존재하지 않는 코스 아이디(" + courseId + ") 입니다."));
            log.debug("Error response from /api/courses/detail: {}", response.getBody());
            return response;
        }
    }

    // Top 코스 리스트
    @GetMapping("/top")
    @Operation(summary = "평균 별점, 방분수, 리뷰 개수 기준으로 상위 코스를 조회한다.", description = "정렬 기준은 지정가능합니다. 지정하지 않으면, 생성일이 기준입니다. 기본 페이지 사이즈는 10이며 0번 페이지부터 시작합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공시 요약 정보들이 전송됨")
    @ApiResponse(responseCode = "500", description = "서버 내부에서 조회에 실패하는 경우임.")
    public ResponseEntity<ResponseRoot<TopRatedCourseResponse>> getTopStarsCourses() {
        log.debug("Request to /api/courses/top");
        TopRatedCourseResponse res = new TopRatedCourseResponse();
        res.setTopStars(courseQueryService.queryTopRatedCourses(CourseQueryService.RecommendationCriteria.STARS));
        res.setTopVisited(courseQueryService.queryTopRatedCourses(CourseQueryService.RecommendationCriteria.TOTAL_VISITATION));
        res.setTopReviewCount(courseQueryService.queryTopRatedCourses(CourseQueryService.RecommendationCriteria.REVIEWS));

        ResponseEntity<ResponseRoot<TopRatedCourseResponse>> response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                .body(CommonResponseBuilder.success("", res));
        log.debug("Response from /api/courses/top: {}", response.getBody());
        return response;
    }

    // 사용자 질병 코스 리스트
    @GetMapping("/disease")
    @Operation(summary = "질병코스 상에서 통계적으로 조회한다.", description = "사용자 질병 코드로 동일 질병 가진 사람들이 방문한 평균 별점이 가장 높은 코스를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공시 추천되는 코스 요약 정보들이 전송됨")
    @ApiResponse(responseCode = "500", description = "서버 내부에서 조회에 실패하는 경우임.")
    public ResponseEntity<ResponseRoot<DiseaseCourseResponse>> getCoursesForDisease(@AuthenticationPrincipal CustomUserDetails user,
                                                                                    @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Long userId = user.getUserId();
        log.debug("Request to /api/courses/disease for user: {}, pageable: {}", userId, pageable);
        List<DiseaseAndCourses> diseaseAndCourses = courseQueryService.queryCoursesForDiseasesOf(userId, CourseQueryService.RecommendationCriteria.STARS, pageable);
        ResponseEntity<ResponseRoot<DiseaseCourseResponse>> response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(
                CommonResponseBuilder.success("", new DiseaseCourseResponse(diseaseAndCourses))
        );
        log.debug("Response from /api/courses/disease: {}", response.getBody());
        return response;
    }

    // insert into health_infos values(null, 1, 1, 175, 70, 25, 1, 13, 2, 1);g
    // 사용자 지역 코스 리스트
    @GetMapping("/user-region")
    @Operation(summary = "사용자 거주 지역 코스를 조회한다.", description = "사용자가 가입시 입력한 거주지 주소 코드로 코스를 조회합니다. 없을 수 있습니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공시 추천되는 코스 요약 정보들이 전송됨")
    @ApiResponse(responseCode = "500", description = "서버 내부에서 조회에 실패하는 경우임.")
    public ResponseEntity<ResponseRoot<CourseResponse>> getUserRegionCourses(@AuthenticationPrincipal CustomUserDetails user,
                                                                             @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Long userId = user.getUserId();
        log.debug("Request to /api/courses/user-region for user: {}, pageable: {}", userId, pageable);
        Profile profile = profileQueryService.queryProfileByUserId(userId);


        List<CourseSummary> summaries = null;
        if(courseQueryService.checkSidoCodeValidation(profile.getSidoCode())) {
            summaries = courseQueryService.queryCoursesByRegion(profile.getSidoCode(), profile.getSigunguCode(), userId, pageable);
        } else {
            summaries = new ArrayList<>();
        }

        ResponseEntity<ResponseRoot<CourseResponse>> response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                .body(CommonResponseBuilder.success("", new CourseResponse(summaries)));
        log.debug("Response from /api/courses/user-region: {}", response.getBody());
        return response;
    }

    // 방문 예정 코스 조회
    @GetMapping("/schedule")
    @Operation(summary = "방문 예정으로 등록했던 기록을 불러옵니다..", description = "사용자가 방문하기로 예정을 등록한 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공시 날짜와 코스 정보들이 전송됨")
    @ApiResponse(responseCode = "500", description = "서버 내부에서 조회에 실패하는 경우임.")
    public ResponseEntity<ResponseRoot<VisitationScheduleResponse>> getCourseSchedule(@AuthenticationPrincipal CustomUserDetails user) {
        log.debug("Request to /api/courses/schedule for user: {}", user.getUserId());
        List<VisitationScheduleSummary> summaries = courseScheduleQueryService.queryAllSchedule(user.getUserId());

        ResponseEntity<ResponseRoot<VisitationScheduleResponse>> response = ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                .body(CommonResponseBuilder.success("", new VisitationScheduleResponse(summaries)));
        log.debug("Response from /api/courses/schedule: {}", response.getBody());
        return response;
    }

    // 코스 리뷰 조회


    // 예정 등록
    @PostMapping("/schedule")
    @Operation(summary = "코스를 방문 예정으로 등록한다.", description = "특정 코스를 특정 날짜에 방문하기로 등록합니다.")
    @ApiResponse(responseCode = "201", description = "등록 성공시 전송됨")
    @ApiResponse(responseCode = "500", description = "서버 내부에서 등록에 실패하는 경우임.")
    public ResponseEntity<ResponseRoot<?>> registNewVisitationSchedule(@AuthenticationPrincipal CustomUserDetails user,
            @RequestBody CourseScheduleRequest request) {
        log.debug("Request to /api/courses/schedule for registration with request: {}", request);
        courseScheduleManageService.registSchedule(request, user.getUserId());

        ResponseEntity<ResponseRoot<?>> response = ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON)
                .body(CommonResponseBuilder.successCreated("코스 방문 예정 등록에 성공했습니다.", null));
        log.debug("Response from /api/courses/schedule for registration: {}", response.getBody());
        return response;
    }

    // 코스 리뷰 조회
    @GetMapping("/reviews")
    @Operation(summary = "코스에 대한 리뷰를 조회합니다.", description = "특정 코스에 대한 리뷰를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공 리뷰 데이터가 전송됨")
    @ApiResponse(responseCode = "500", description = "서버 내부에서 조회에 실패하는 경우임.")
    public ResponseEntity<ResponseRoot<VisitationScheduleResponse>> getCourseReviews(@RequestParam("courseId") Long courseId) {
        return null;
//        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
//                .body(CommonResponseBuilder.success("", new VisitationScheduleResponse(summaries)));
    }
}
