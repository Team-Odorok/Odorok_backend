package com.odorok.OdorokApplication.diary.controller;

import com.odorok.OdorokApplication.commons.exception.BadRequestException;
import com.odorok.OdorokApplication.commons.exception.NotFoundException;
import com.odorok.OdorokApplication.commons.response.ResponseRoot;
import com.odorok.OdorokApplication.diary.dto.request.DiaryChatAnswerRequest;
import com.odorok.OdorokApplication.diary.dto.request.DiaryRegenerationRequest;
import com.odorok.OdorokApplication.diary.dto.request.DiaryRequest;
import com.odorok.OdorokApplication.diary.dto.response.*;
import com.odorok.OdorokApplication.diary.service.DiaryService;
import com.odorok.OdorokApplication.security.dto.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static com.odorok.OdorokApplication.commons.response.CommonResponseBuilder.*;

@RequestMapping("/api/diaries")
@RequiredArgsConstructor
@RestController
@Slf4j
public class DiaryController {

    private final DiaryService diaryService;
    private final String DIARY_LIST_GROUPING_BY = "year";

    @GetMapping()
    public ResponseEntity<?> searchAllDiaryByUserId(@RequestParam(required = false) String groupBy, @AuthenticationPrincipal CustomUserDetails user) {
        long userId = user.getUserId();
        ResponseRoot<?> response;
        if(groupBy != null && groupBy.equals(DIARY_LIST_GROUPING_BY)) {
            // 연도 기준으로 리스트 리턴
            Map<String, List<DiarySummary>> diaryListGroupByYear = diaryService.findAllDiaryGroupByYear(userId);
            response = success("연도별 일지 목록 조회 성공", diaryListGroupByYear);
        } else {
            List<DiarySummary> diaryList = diaryService.findAllDiaryByUser(userId);
            response = success("일지 목록 조회 성공", diaryList);
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{diaryId}")
    public ResponseEntity<?> searchDiaryById(@PathVariable long diaryId, @AuthenticationPrincipal CustomUserDetails user) {
        long userId = user.getUserId();
        DiaryDetail diary = diaryService.findDiaryById(userId, diaryId);
        if(diary == null) {
            throw new NotFoundException("해당 일지를 찾을 수 없습니다.");
        }

        ResponseRoot<DiaryDetail> response = success("일지 상세 조회 성공", diary);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/permission")
    public ResponseEntity<?> searchDiaryGeneratePermission(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        long userId = user.getUserId();
        DiaryPermissionCheckResponse response = diaryService.findDiaryPermission(userId);
        return ResponseEntity.status(HttpStatus.OK).body(success("일지 생성 가능 조회 성공", response));
    }

    @GetMapping("/generation/{visitedCourseId}")
    public ResponseEntity<?> registGeneration(@RequestParam String style,
                                              @PathVariable Long visitedCourseId,
                                              @AuthenticationPrincipal CustomUserDetails user
    ) {
        if (style == null || style.isBlank()) {
            throw new BadRequestException("스타일은 필수 입력값입니다.");
        }
        long userId = user.getUserId();

        DiaryChatResponse chatResponse =  diaryService.insertGeneration(userId, style, visitedCourseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(success("IN_PROGRESS", "일지 생성 요청 성공", chatResponse));
    }

    @PostMapping("/answers")
    public ResponseEntity<?> registAnswer(@RequestBody DiaryChatAnswerRequest request
            ,@AuthenticationPrincipal CustomUserDetails user
    ) {
        long userId = user.getUserId();
        DiaryChatResponse chatResponse = diaryService.insertAnswer(userId, request);
        ResponseRoot<DiaryChatResponse> response = chatResponse.getContent().endsWith("<END>") ?
                successDone("일지 생성 완료", chatResponse) :
                successInProgress("답변 제출 및 새 질문 요청 성공", chatResponse);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @PostMapping("/regeneration")
    public ResponseEntity<?> registRegeneration(@RequestBody DiaryRegenerationRequest request
            ,@AuthenticationPrincipal CustomUserDetails user
    ) {
        long userId = user.getUserId();
        DiaryChatResponse chatResponse = diaryService.insertRegeneration(userId, request);
        ResponseRoot<DiaryChatResponse> response = successDone("일지 재생성 완료", chatResponse);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/available-course")
    public ResponseEntity<?> searchVisitedCourseWithoutDiary(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        long userId = user.getUserId();
        VisitedCourseWithoutDiaryResponse visitedCourseList = diaryService.findVisitedCourseWithoutDiaryByUserId(userId);
        ResponseRoot<VisitedCourseWithoutDiaryResponse> response = success("일지 생성 안 된 방문 완료 코스 조회 성공", visitedCourseList);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> registFinalizeDiary(
            @RequestPart("diary") @Valid DiaryRequest diaryRequest,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal CustomUserDetails user) {
                long userId = user.getUserId();
        Long savedDiaryId = diaryService.insertFinalizeDiary(userId, diaryRequest, images);
        ResponseRoot<Map> response = successCreated("일지 생성 성공", Map.of("diaryId", savedDiaryId));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{diaryId}")
    public ResponseEntity<?> deleteDiary(@PathVariable Long diaryId, @AuthenticationPrincipal CustomUserDetails user) {
        long userId = user.getUserId();
        diaryService.deleteDiaryById(userId, diaryId);
        ResponseRoot<?> response = success("일지 삭제 성공");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/diary-create-items")
    public ResponseEntity<?> getDiaryCreatedItem(@RequestParam Integer quantity, @AuthenticationPrincipal CustomUserDetails user) {
        long userId = user.getUserId();
        diaryService.purchaseDiaryPermissionItem(userId, quantity);
        ResponseRoot<?> response = success("일지 아이템 구매 성공");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
