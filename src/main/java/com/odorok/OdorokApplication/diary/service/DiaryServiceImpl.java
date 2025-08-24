package com.odorok.OdorokApplication.diary.service;

import com.odorok.OdorokApplication.commons.exception.GptCommunicationException;
import com.odorok.OdorokApplication.commons.exception.NotFoundException;
import com.odorok.OdorokApplication.diary.dto.request.DiaryRegenerationRequest;
import com.odorok.OdorokApplication.diary.dto.request.DiaryRequest;
import com.odorok.OdorokApplication.diary.dto.response.*;
import com.odorok.OdorokApplication.diary.repository.VisitedCourseRepository;
import com.odorok.OdorokApplication.diary.dto.gpt.VisitedCourseAndAttraction;
import com.odorok.OdorokApplication.diary.dto.request.DiaryChatAnswerRequest;
import com.odorok.OdorokApplication.diary.repository.DiaryRepository;
import com.odorok.OdorokApplication.diary.util.PromptTemplate;
import com.odorok.OdorokApplication.domain.Diary;
import com.odorok.OdorokApplication.domain.DiaryImage;
import com.odorok.OdorokApplication.draftDomain.Inventory;
import com.odorok.OdorokApplication.draftDomain.Item;
import com.odorok.OdorokApplication.gpt.service.GptService;
import com.odorok.OdorokApplication.diary.repository.InventoryRepository;
import com.odorok.OdorokApplication.diary.repository.ItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class DiaryServiceImpl implements DiaryService{
    private final GptService gptService;
    private final DiaryRepository diaryRepository;
    private final InventoryRepository inventoryRepository;
    private final ItemRepository itemRepository;
    private final VisitedCourseRepository visitedCourseRepository;
    private final DiaryImageService diaryImageService;

    private Long diaryPermissionItemId;

    @Value("${gpt.system-prompt}")
    private String rawSystemPrompt;

    @Value("${gpt.regeneration-prompt}")
    private String regenerationPrompt;

    @Value("${gpt.default-feedback-prompt}")
    private String defaultFeedbackPrompt;

    @Value("${gpt.generation-finalize-diary-prompt}")
    private String generationFinalizeDiaryPrompt;
    // 일지 생성권 itemId 캐싱.
    private Long getDiaryPermissionItemId() {
        if (diaryPermissionItemId == null) {
            diaryPermissionItemId = itemRepository.findByTitle("일지 생성권")
                    .map(Item::getId)
                    .orElseThrow(() -> new IllegalStateException(
                            "'일지 생성권' 아이템이 DB에 없습니다. 초기 데이터 삽입을 확인하세요."
                    ));
        }
        return diaryPermissionItemId;
    }

    @Override
    public List<DiarySummary> findAllDiaryByUser(long userId) {
        return diaryRepository.findDiaryByUserId(userId);
    }

    @Override
    public Map<String, List<DiarySummary>> findAllDiaryGroupByYear(long userId) {
        List<DiarySummary> diaryList = diaryRepository.findDiaryByUserId(userId);
        return diaryList.stream()
                .sorted(Comparator.comparing(DiarySummary::getCreatedAt).reversed())
                .collect(Collectors.groupingBy(
                        diary -> String.valueOf(diary.getCreatedAt().getYear()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    @Override
    public DiaryDetail findDiaryById(long userId, long diaryId){
        return diaryRepository.findDiaryById(userId, diaryId);
    }

    @Override
    public DiaryPermissionCheckResponse findDiaryPermission(long userId) {
        Long itemId = getDiaryPermissionItemId();
        Inventory inventory = inventoryRepository.findByUserIdAndItemId(userId, itemId)
                .orElseGet(() -> Inventory.builder()
                        .userId(userId)
                        .itemId(itemId)
                        .count(0)
                        .build());
        return new DiaryPermissionCheckResponse(inventory.getUserId(), inventory.getItemId(), inventory.getCount());
    }

    @Override
    @Transactional
    public void deleteDiaryById(long userId, long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> {
                    log.warn("일지 삭제 실패: 존재하지 않는 일지. diaryId={}, userId={}", diaryId, userId);
                    return new IllegalArgumentException("존재하지 않는 일지");
                });

        if (diary.getUserId() != userId) {
            log.warn("일지 삭제 실패: 권한 없음. diaryId={}, userId={}", diaryId, userId);
            throw new AccessDeniedException("본인이 작성한 일지만 삭제할 수 있음");
        }
        // 다이어리 이미지 삭제
        List<DiaryImage> diaryImages = Optional.ofNullable(diaryImageService.getDiaryImages(diaryId))
                .orElse(Collections.emptyList());
        List<String> imgUrls = diaryImages.stream()
                .map(DiaryImage::getImgUrl)
                .collect(Collectors.toList());

        log.info("일지 이미지 삭제 시작: diaryId={}, imageCount={}", diaryId, imgUrls.size());
        diaryImageService.deleteDiaryImages(imgUrls);

        // 다이어리 삭제
        diaryRepository.deleteById(diaryId);
        log.info("일지 삭제 완료: diaryId={}, userId={}", diaryId, userId);
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
        Long itemId = getDiaryPermissionItemId();
        Inventory inventory = inventoryRepository.findByUserIdAndItemId(userId, itemId)
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

    @Override
    @Transactional
    public Long insertFinalizeDiary(long userId, DiaryRequest diaryRequest, List<MultipartFile> images) {
        // s3에 이미지 등록
        List<String> imageUrls = diaryImageService.insertDiaryImage(images, userId);
        try {
            String finalDiaryContent = getFinalizeContentWithMarkup(diaryRequest.getContent());
            // 일지 title, content 등록
            Diary diary = Diary.builder()
                    .title(diaryRequest.getTitle())
                    .vcourseId(diaryRequest.getVcourseId())
                    .content(finalDiaryContent)
                    .userId(userId)
                    .build();
            Diary savedDiary = diaryRepository.save(diary);

            // 일지 이미지 url db에 등록
            diaryImageService.insertDiaryImageUrl(imageUrls, savedDiary.getId());

            return savedDiary.getId();
        } catch (Exception e) {
            diaryImageService.deleteDiaryImages(imageUrls);
            log.error("일지 DB 등록 중 예외 발생 - 이미지 정리 후 예외 전파", e);
            throw e;
        }
    }

    public String getFinalizeContentWithMarkup(String content) {
        String requestDiary = PromptTemplate.of(generationFinalizeDiaryPrompt)
                .with("diaryContent", content)
                .build();
        log.debug("최종 일지 내용을 markup으로 수정하기 위한 요청 프롬프트: {} ", requestDiary);
        GptService.Prompt prompt = GptService.Prompt.builder()
                .role("user")
                .content(requestDiary)
                .build();
        List<GptService.Prompt> chatLog = gptService.sendPrompt(null, prompt);
        if (chatLog.isEmpty()) {
            log.warn("최종 일지 요청 후 GPT 응답이 비어 있음. prompt: {}", prompt);
            throw new GptCommunicationException("GPT 응답이 비어있음 ");
        }
        return chatLog.get(chatLog.size() - 1).getContent();
    }
}