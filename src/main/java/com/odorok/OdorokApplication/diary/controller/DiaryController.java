package com.odorok.OdorokApplication.diary.controller;

import com.odorok.OdorokApplication.commons.exception.BadRequestException;
import com.odorok.OdorokApplication.commons.exception.NotFoundException;
import com.odorok.OdorokApplication.commons.response.ResponseRoot;
import com.odorok.OdorokApplication.diary.dto.response.DiaryChatResponse;
import com.odorok.OdorokApplication.diary.dto.response.DiaryDetail;
import com.odorok.OdorokApplication.diary.dto.response.DiaryPermissionCheckResponse;
import com.odorok.OdorokApplication.diary.service.DiaryService;
import com.odorok.OdorokApplication.security.principal.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> searchDiaryGeneratePermission(@AuthenticationPrincipal CustomUserDetails user) {
        long userId = user.getUser().getId();
        DiaryPermissionCheckResponse response = diaryService.findDiaryPermission(userId);
        return ResponseEntity.status(HttpStatus.OK).body(success("일지 생성 가능 조회 성공", response));
    }

    @GetMapping("/generation/{visitedCourseId}")
    public ResponseEntity<?> registGeneration(@RequestParam String style,
                                              @PathVariable Long visitedCourseId,
                                              @AuthenticationPrincipal CustomUserDetails user) {
        if (style == null || style.isBlank()) {
            throw new BadRequestException("스타일은 필수 입력값입니다.");
        }

        long userId = user.getUser().getId();

        DiaryChatResponse charResponse =  diaryService.insertGeneration(userId, style, visitedCourseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(success("IN_PROGRESS", "일지 생성 요청 성공", charResponse));
    }

}
