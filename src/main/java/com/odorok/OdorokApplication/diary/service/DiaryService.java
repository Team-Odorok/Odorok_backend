package com.odorok.OdorokApplication.diary.service;

import com.odorok.OdorokApplication.commons.exception.GptCommunicationException;
import com.odorok.OdorokApplication.diary.dto.request.DiaryChatAnswerRequest;
import com.odorok.OdorokApplication.diary.dto.request.DiaryRegenerationRequest;
import com.odorok.OdorokApplication.diary.dto.response.*;
import com.odorok.OdorokApplication.gpt.service.GptService;

import java.util.List;

public interface DiaryService {
//    public List<DiarySummary> findAllDiaryByMember(long userId);
    public DiaryDetail findDiaryById(long userId, long diaryId);
    public DiaryPermissionCheckResponse findDiaryPermission(long userId);
    public DiaryChatResponse insertGeneration(long userId, String style, Long visitedCoursesId) throws GptCommunicationException;
    public GptService.Prompt buildFinalSystemPrompt(long userId, String style, Long visitedCoursesId);
    public void decreaseDiaryGenerationItemCount(Long userId);
    public DiaryChatResponse insertAnswer(long userId, DiaryChatAnswerRequest request);
    public VisitedCourseWithoutDiaryResponse findVisitedCourseWithoutDiaryByUserId(long userId);
    public DiaryChatResponse insertRegeneration(long userId, DiaryRegenerationRequest request);
    public GptService.Prompt buildRegenerationPrompt(String feedback);
}
