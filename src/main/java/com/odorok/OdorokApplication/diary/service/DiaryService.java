package com.odorok.OdorokApplication.diary.service;

import com.odorok.OdorokApplication.commons.exception.GptCommunicationException;
import com.odorok.OdorokApplication.diary.dto.request.DiaryChatAnswerRequest;
import com.odorok.OdorokApplication.diary.dto.request.DiaryRegenerationRequest;
import com.odorok.OdorokApplication.diary.dto.request.DiaryRequest;
import com.odorok.OdorokApplication.diary.dto.response.*;
import com.odorok.OdorokApplication.gpt.service.GptService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface DiaryService {
    public List<DiarySummary> findAllDiaryByUser(long userId);

    public Map<String, List<DiarySummary>> findAllDiaryGroupByYear(long userId);

    public DiaryDetail findDiaryById(long userId, long diaryId);

    public void deleteDiaryById(long userId, long diaryId);

    public DiaryPermissionCheckResponse findDiaryPermission(long userId);

    public DiaryChatResponse insertGeneration(long userId, String style, Long visitedCoursesId) throws GptCommunicationException;

    public GptService.Prompt buildFinalSystemPrompt(long userId, String style, Long visitedCoursesId);

    public void decreaseDiaryGenerationItemCount(Long userId);

    public DiaryChatResponse insertAnswer(long userId, DiaryChatAnswerRequest request);

    public VisitedCourseWithoutDiaryResponse findVisitedCourseWithoutDiaryByUserId(long userId);

    public DiaryChatResponse insertRegeneration(long userId, DiaryRegenerationRequest request);

    public GptService.Prompt buildRegenerationPrompt(String feedback);

    public Long insertFinalizeDiary(long userId, DiaryRequest diaryRequest, List<MultipartFile> images);

    public void purchaseDiaryPermissionItem(long userId, int quantity);
}
