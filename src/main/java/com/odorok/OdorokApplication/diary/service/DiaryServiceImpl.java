package com.odorok.OdorokApplication.diary.service;

import com.odorok.OdorokApplication.commons.exception.GptCommunicationException;
import com.odorok.OdorokApplication.commons.exception.NotFoundException;
import com.odorok.OdorokApplication.diary.dto.request.DiaryRegenerationRequest;
import com.odorok.OdorokApplication.diary.dto.response.*;
import com.odorok.OdorokApplication.diary.repository.VisitedCourseRepository;
import com.odorok.OdorokApplication.diary.dto.gpt.VisitedCourseAndAttraction;
import com.odorok.OdorokApplication.diary.dto.request.DiaryChatAnswerRequest;
import com.odorok.OdorokApplication.diary.repository.DiaryRepository;
import com.odorok.OdorokApplication.diary.repository.VisitedCourseRepository;
import com.odorok.OdorokApplication.diary.util.PromptTemplate;
import com.odorok.OdorokApplication.draftDomain.Inventory;
import com.odorok.OdorokApplication.draftDomain.Item;
import com.odorok.OdorokApplication.gpt.service.GptService;
import com.odorok.OdorokApplication.diary.repository.InventoryRepository;
import com.odorok.OdorokApplication.diary.repository.ItemRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class DiaryServiceImpl implements DiaryService{
    private final GptService gptService;
    private final DiaryRepository diaryRepository;
    private final InventoryRepository inventoryRepository;
    private final ItemRepository itemRepository;
    private final VisitedCourseRepository visitedCourseRepository;

    private Long diaryPermissionItemId;

    @Value("${gpt.system-prompt}")
    private String rawSystemPrompt;

    @Value("${gpt.regeneration-prompt}")
    private String regenerationPrompt;

    @Value("${gpt.default-feedback-prompt}")
    private String defaultFeedbackPrompt;

    // 일지 생성권 itemId 캐싱.
    @PostConstruct
    public void initDiaryItemId() {
        this.diaryPermissionItemId = itemRepository.findByTitle("일지 생성권")
                .map(Item::getId)
                .orElseThrow(() ->
                        new IllegalStateException("'일지 생성권' 아이템이 DB에 없습니다. 서버 실행 중단. 데이터 삽입을 확인하세요.")
                );
    }

    @Override
    public DiaryDetail findDiaryById(long userId, long diaryId){
        return diaryRepository.findDiaryById(userId, diaryId);
    }

    @Override
    public DiaryPermissionCheckResponse findDiaryPermission(long userId) {
        diaryPermissionItemId = 3L;
        Inventory inventory = inventoryRepository.findByUserIdAndItemId(userId, diaryPermissionItemId)
                .orElseGet(() -> Inventory.builder()
                        .userId(userId)
                        .itemId(diaryPermissionItemId)
                        .count(0)
                        .build());
        return new DiaryPermissionCheckResponse(inventory.getUserId(), inventory.getItemId(), inventory.getCount());
    }

    @Override
    @Transactional
    public DiaryChatResponse insertGeneration(long userId, String style, Long visitedCoursesId) throws GptCommunicationException {
        // 시스템 프롬프트 설정
        GptService.Prompt prompt = buildFinalSystemPrompt(userId, style, visitedCoursesId);

        // 일지 생성권 차감
        decreaseDiaryGenerationItemCount(userId);

        // 대화 시작
        List<GptService.Prompt> chatLog = gptService.sendPrompt(null, prompt);
        if (chatLog.isEmpty()) {
            log.warn("GPT 응답이 비어 있음. prompt: {}", prompt);
            throw new RuntimeException("GPT 응답이 비어있음 ");
        }
        String newContent = chatLog.get(chatLog.size() - 1).getContent();
        return new DiaryChatResponse(newContent, chatLog);
    }

    public GptService.Prompt buildFinalSystemPrompt(long userId, String style, Long visitedCoursesId) {
        // 방문한 코스 / 명소 조회
        VisitedCourseAndAttraction visited = visitedCourseRepository.findCourseAndAttractionsByVisitedCourseId(userId, visitedCoursesId);
        if(visited == null) {
            throw new NotFoundException("유효한 vcourse id가 아닙니다.");
        }
        // 시스템 프롬프트에 방문 장소, 명소 등 데이터 추가
        String systemPrompt = PromptTemplate.of(rawSystemPrompt)
                .with("style", style)
                .with("courseName", visited.getCourseName())
                .with("courseSummary", visited.getCourseSummary())
                .with("additionalAttractions", visited.attractionsToString())
                .build();
        log.debug("!!!_________visited_Attractions: {}", visited.attractionsToString());
        return new GptService.Prompt("system", systemPrompt);
    }

    public void decreaseDiaryGenerationItemCount(Long userId) {
        Inventory inventory = inventoryRepository.findByUserIdAndItemId(userId, diaryPermissionItemId)
                .orElseThrow(() -> new NotFoundException("생성권 아이템이 없습니다."));

        if (inventory.getCount() <= 0) {
            throw new IllegalStateException("생성권 아이템 수량이 부족합니다.");
        }
        inventory.setCount(inventory.getCount() - 1);
    }

    @Override
    public DiaryChatResponse insertAnswer(long userId, DiaryChatAnswerRequest request) {
        // answer로 프롬프트 생성
        GptService.Prompt newPrompt = new GptService.Prompt("user", request.getAnswer());
        List<GptService.Prompt> chatLog = gptService.sendPrompt(request.getChatLog(), newPrompt);
        if (chatLog.isEmpty()) {
            log.warn("GPT 응답이 비어 있음. chatLog: {}\nprompt: {}", chatLog, newPrompt);
            throw new GptCommunicationException("GPT 응답이 비어있음 ");
        }
        String newContent = chatLog.get(chatLog.size() - 1).getContent();
        return new DiaryChatResponse(newContent, chatLog);
    }

    @Override
    public VisitedCourseWithoutDiaryResponse findVisitedCourseWithoutDiaryByUserId(long userId) {
        List<VisitedCourseSummary> result = visitedCourseRepository.findVisitedCourseWithoutDiaryByUserId(userId);
        return new VisitedCourseWithoutDiaryResponse(result);
    }

    @Override
    public DiaryChatResponse insertRegeneration(long userId, DiaryRegenerationRequest request) {
        // answer로 프롬프트 생성
        String feedback = request.getFeedback();
        if(feedback == null) {
            // 피드백이 없는 경우 들어갈 기본 피드백
            feedback = defaultFeedbackPrompt;
        }
        GptService.Prompt newPrompt = buildRegenerationPrompt(feedback);
        List<GptService.Prompt> chatLog = gptService.sendPrompt(request.getChatLog(), newPrompt);
        if (chatLog.isEmpty()) {
            log.warn("GPT 응답이 비어 있음. chatLog: {}\nprompt: {}", chatLog, newPrompt);
            throw new GptCommunicationException("GPT 응답이 비어있음 ");
        }
        String newContent = chatLog.get(chatLog.size() - 1).getContent();
        if(!newContent.endsWith("<END>")) {
            throw new GptCommunicationException("GPT 응답에 <END> 토큰이 누락");
        }
        return new DiaryChatResponse(newContent, chatLog);
    }

    public GptService.Prompt buildRegenerationPrompt(String feedback) {
        String regenerationPromptWithFeedback = PromptTemplate.of(regenerationPrompt)
                .with("feedback", feedback)
                .build();
        return new GptService.Prompt("user", regenerationPromptWithFeedback);
    }
}
