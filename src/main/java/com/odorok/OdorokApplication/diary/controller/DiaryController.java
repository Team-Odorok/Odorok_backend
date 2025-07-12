package com.odorok.OdorokApplication.diary.controller;

import com.odorok.OdorokApplication.commons.exception.BadRequestException;
import com.odorok.OdorokApplication.commons.exception.NotFoundException;
import com.odorok.OdorokApplication.commons.response.ResponseRoot;
import com.odorok.OdorokApplication.diary.dto.request.DiaryChatAnswerRequest;
import com.odorok.OdorokApplication.diary.dto.request.DiaryRegenerationRequest;
import com.odorok.OdorokApplication.diary.dto.response.DiaryChatResponse;
import com.odorok.OdorokApplication.diary.dto.response.DiaryDetail;
import com.odorok.OdorokApplication.diary.dto.response.DiaryPermissionCheckResponse;
import com.odorok.OdorokApplication.diary.dto.response.VisitedCourseWithoutDiaryResponse;
import com.odorok.OdorokApplication.diary.service.DiaryService;
import com.odorok.OdorokApplication.security.principal.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.odorok.OdorokApplication.commons.response.CommonResponseBuilder.*;

@RequestMapping("/api/diaries")
@RequiredArgsConstructor
@RestController
@Slf4j
public class DiaryController {

    private final DiaryService diaryService;

    @GetMapping("/{diaryId}")
    public ResponseEntity<?> searchDiaryById(@PathVariable long diaryId, @AuthenticationPrincipal CustomUserDetails user) {
        long userId = user.getUser().getId();
        DiaryDetail diary = diaryService.findDiaryById(userId, diaryId);
        if(diary == null) {
            throw new NotFoundException("해당 일지를 찾을 수 없습니다.");
        }

        ResponseRoot<DiaryDetail> response = success("일지 상세 조회 성공", diary);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/permission")
    public ResponseEntity<?> searchDiaryGeneratePermission(
//            @AuthenticationPrincipal CustomUserDetails user
    ) {
//        long userId = user.getUser().getId();
        long userId = 1L; // 테스트용
        DiaryPermissionCheckResponse response = diaryService.findDiaryPermission(userId);
        return ResponseEntity.status(HttpStatus.OK).body(success("일지 생성 가능 조회 성공", response));
    }

    @GetMapping("/generation/{visitedCourseId}")
    public ResponseEntity<?> registGeneration(@RequestParam String style,
                                              @PathVariable Long visitedCourseId
//                                              ,@AuthenticationPrincipal CustomUserDetails user
    ) {
        if (style == null || style.isBlank()) {
            throw new BadRequestException("스타일은 필수 입력값입니다.");
        }
//        long userId = user.getUser().getId();
        long userId = 1L; // 테스트용

        DiaryChatResponse chatResponse =  diaryService.insertGeneration(userId, style, visitedCourseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(success("IN_PROGRESS", "일지 생성 요청 성공", chatResponse));
    }

    @PostMapping("/answers")
    public ResponseEntity<?> registAnswer(@RequestBody DiaryChatAnswerRequest request
//            ,@AuthenticationPrincipal CustomUserDetails user
    ) {
//        long userId = user.getUser().getId();
        long userId = 1L; // 테스트용
        DiaryChatResponse chatResponse = diaryService.insertAnswer(userId, request);
        ResponseRoot<DiaryChatResponse> response = chatResponse.getContent().endsWith("<END>") ?
                successDone("일지 생성 완료", chatResponse) :
                successInProgress("답변 제출 및 새 질문 요청 성공", chatResponse);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @PostMapping("/regeneration")
    public ResponseEntity<?> registRegeneration(@RequestBody DiaryRegenerationRequest request
//            ,@AuthenticationPrincipal CustomUserDetails user
    ) {
        long userId = 1L;
        DiaryChatResponse chatResponse = diaryService.insertRegeneration(userId, request);
        ResponseRoot<DiaryChatResponse> response = successDone("일지 재생성 완료", chatResponse);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/available-course")
    public ResponseEntity<?> searchVisitedCourseWithoutDiary(
//            @AuthenticationPrincipal CustomUserDetails user;
    ) {
//        long userId = user.getUser().getId();
        long userId = 1L; // 테스트용
        VisitedCourseWithoutDiaryResponse response = diaryService.findVisitedCourseWithoutDiaryByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
