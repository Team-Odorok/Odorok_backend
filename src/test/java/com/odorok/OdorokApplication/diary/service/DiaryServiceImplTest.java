package com.odorok.OdorokApplication.diary.service;

import com.odorok.OdorokApplication.commons.exception.NotFoundException;
import com.odorok.OdorokApplication.diary.dto.gpt.VisitedAdditionalAttraction;
import com.odorok.OdorokApplication.diary.dto.gpt.VisitedCourseAndAttraction;
import com.odorok.OdorokApplication.diary.dto.response.DiaryChatResponse;
import com.odorok.OdorokApplication.diary.dto.response.DiaryDetail;
import com.odorok.OdorokApplication.diary.dto.response.DiaryPermissionCheckResponse;
import com.odorok.OdorokApplication.diary.repository.DiaryRepository;
import com.odorok.OdorokApplication.draftDomain.Inventory;
import com.odorok.OdorokApplication.gpt.service.GptService;
import com.odorok.OdorokApplication.diary.repository.InventoryRepository;
import com.odorok.OdorokApplication.diary.repository.VisitedCourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.Nested;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class DiaryServiceImplTest {
    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private DiaryServiceImpl diaryService;

    @BeforeEach
    void setUp() {
        //
        ReflectionTestUtils.setField(diaryService, "diaryPermissionItemId", 3L);
    }

    @Test
    public void 일지_상세정보_조회_성공() {
        // given
        long userId = 1L;
        long diaryId = 2L;

        DiaryDetail mockDiaryDetail = new DiaryDetail(
                diaryId,
                "제목",
                "내용",
                List.of("img1.jpg", "img2.jpg"),
                userId,
                "제주도 코스",
                LocalDateTime.of(2024, 6, 1, 10, 0),
                LocalDateTime.of(2024, 6, 1, 12, 0)
        );

        when(diaryRepository.findDiaryById(userId, diaryId)).thenReturn(mockDiaryDetail);

        // when
        DiaryDetail result = diaryService.findDiaryById(userId, diaryId);

        // then
        assertNotNull(result);
        assertEquals("제목", result.getTitle());
        assertEquals(userId, result.getUserId());
        assertEquals("제주도 코스", result.getCourseName());
        assertEquals(2, result.getImgs().size());
        verify(diaryRepository, times(1)).findDiaryById(userId, diaryId);

    }

    @Test
    void 일지_생성권_조회_생성권이_있을_때_성공() {
        // given
        long userId = 1L;
        long itemId = 3L;

        ReflectionTestUtils.setField(diaryService, "diaryPermissionItemId", itemId);

        Inventory inventory = Inventory.builder()
                .userId(userId)
                .itemId(itemId)
                .count(2)
                .build();

        when(inventoryRepository.findByUserIdAndItemId(userId, itemId)).thenReturn(Optional.of(inventory));

        // when
        DiaryPermissionCheckResponse response = diaryService.findDiaryPermission(userId);

        //then
        assertEquals(userId, response.getUserId());
        assertEquals(itemId, response.getItemId());
        assertEquals(2, response.getCount());
        assertTrue(response.isCanCreateDiary());
    }

    @Test
    void 일지_생성권_조회_생성권이_없을_때_성공() {
        // given
        long userId = 2L;
        long itemId = 3L;

        ReflectionTestUtils.setField(diaryService, "diaryPermissionItemId", itemId);

        Inventory inventory = Inventory.builder()
                .userId(userId)
                .itemId(itemId)
                .count(0)
                .build();

        when(inventoryRepository.findByUserIdAndItemId(userId, itemId)).thenReturn(Optional.of(inventory));
        // when
        DiaryPermissionCheckResponse response = diaryService.findDiaryPermission(userId);

        //then
        assertEquals(userId, response.getUserId());
        assertEquals(itemId, response.getItemId());
        assertEquals(0, response.getCount());
        assertFalse(response.isCanCreateDiary());
    }

    @Test
    void 일지생성권_차감_일지생성권이_있고_수량이_1개_이상이면_성공() {
        long userId = 1L;
        long itemId = 3L;

        Inventory inventory = Inventory.builder()
                .userId(userId)
                .itemId(itemId)
                .count(2)
                .build();

        when(inventoryRepository.findByUserIdAndItemId(userId, itemId))
                .thenReturn(Optional.of(inventory));

        diaryService.decreaseDiaryGenerationItemCount(userId);

        assertEquals(1, inventory.getCount());
    }

    @Test
    void 일지생성권_차감_생성권이_없으면_NotFoundException_발생() {
        long userId = 1L;
        long itemId = 3L;
        when(inventoryRepository.findByUserIdAndItemId(userId, itemId))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(NotFoundException.class, () ->
                diaryService.decreaseDiaryGenerationItemCount(userId));
        assertEquals("생성권 아이템이 없습니다.", ex.getMessage());

    }

    @Test
    void 일지생성권_차감_생성권_수량이_0이면_IllegalStateException_발생() {
        long userId = 1L;
        long itemId = 3L;
        Inventory inventory = Inventory.builder()
                .userId(userId)
                .itemId(itemId)
                .count(0)
                .build();

        when(inventoryRepository.findByUserIdAndItemId(userId, itemId))
                .thenReturn(Optional.of(inventory));

        RuntimeException ex = assertThrows(IllegalStateException.class, () ->
                diaryService.decreaseDiaryGenerationItemCount(userId));
        assertEquals("생성권 아이템 수량이 부족합니다.", ex.getMessage());
    }

    @Nested
    class InsertGenerationTests {
        @Mock
        private VisitedCourseRepository visitedCourseRepository;

        @Mock
        private GptService gptService;

        @Mock
        private InventoryRepository inventoryRepository;

        @InjectMocks
        private DiaryServiceImpl diaryService;

        private final long userId = 1L;
        private final long itemId = 3L;
        private final long visitedCourseId = 123L;
        private final String style = "감성적인";

        private final String rawPromptTemplate =
                "당신은 여행 기록 작가입니다. 여행자의 스타일은 {style}, 코스명은 {courseName}, 설명은 {courseSummary}, 주변 명소는 {additionalAttractions}입니다.";
        private List<GptService.Prompt> chatLog;
        private GptService.Prompt systemPrompt;


        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(diaryService, "diaryPermissionItemId", itemId);
            ReflectionTestUtils.setField(diaryService, "rawSystemPrompt",rawPromptTemplate);

            // 프롬프트 생성을 위한 mock 방문지 정보 설정
            VisitedAdditionalAttraction a1 = new VisitedAdditionalAttraction("해운대", "부산 해운대구", "명소1");
            VisitedAdditionalAttraction a2 = new VisitedAdditionalAttraction("광안리", "부산 수영구", "명소2");

            VisitedCourseAndAttraction course = VisitedCourseAndAttraction.builder()
                    .courseName("남파랑길 1코스")
                    .courseSummary("해안 절경과 역사적인 장소를 체험할 수 있는 코스")
                    .visitedAttractions(List.of(a1, a2))
                    .build();

            when(visitedCourseRepository.findCourseAndAttractionsByVisitedCourseId(userId, visitedCourseId))
                    .thenReturn(course);

            // 프롬프트 예시
            systemPrompt = diaryService.buildFinalSystemPrompt(userId, style, visitedCourseId);

            // GPT 응답 mock
            chatLog = List.of(
                    systemPrompt,
                    new GptService.Prompt("assistant", "오늘 여행은 어땠나요?")
            );

            Inventory inventory = Inventory.builder()
                    .userId(userId)
                    .itemId(itemId)
                    .count(2)
                    .build();

            when(inventoryRepository.findByUserIdAndItemId(userId, itemId))
                    .thenReturn(Optional.of(inventory));
        }

        @Test
        void 일지_생성_시작_정상응답_성공() {
            // given
            when(gptService.sendPrompt(isNull(), any(GptService.Prompt.class))).thenReturn(chatLog);

            // when
            DiaryChatResponse response = diaryService.insertGeneration(userId, style, visitedCourseId);

            //then
            assertNotNull(response);
            assertEquals("오늘 여행은 어땠나요?", response.getQuestion());
            assertEquals(2, response.getChatLog().size());
        }

        @Test
        void 일지_생성_시작_응답없음_예외발생() {
            // given
            when(gptService.sendPrompt(isNull(), any(GptService.Prompt.class))).thenReturn(List.of());

            // when & then
            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    diaryService.insertGeneration(userId, style, visitedCourseId));

            assertEquals("GPT 응답이 비어있음 ", ex.getMessage());
        }
    }


}
