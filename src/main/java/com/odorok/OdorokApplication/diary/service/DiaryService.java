package com.odorok.OdorokApplication.diary.service;

import com.odorok.OdorokApplication.commons.exception.GptCommunicationException;
import com.odorok.OdorokApplication.diary.dto.response.DiaryChatResponse;
import com.odorok.OdorokApplication.diary.dto.response.DiaryDetail;
import com.odorok.OdorokApplication.diary.dto.response.DiaryPermissionCheckResponse;
import com.odorok.OdorokApplication.gpt.service.GptService;

public interface DiaryService {
//    public List<DiarySummary> findAllDiaryByMember(long userId);
    public DiaryDetail findDiaryById(long userId, long diaryId);
    public DiaryPermissionCheckResponse findDiaryPermission(long userId);
    public DiaryChatResponse insertGeneration(long userId, String style, Long visitedCoursesId) throws GptCommunicationException;
    public GptService.Prompt buildFinalSystemPrompt(long userId, String style, Long visitedCoursesId);
    public void decreaseDiaryGenerationItemCount(Long userId);
}
