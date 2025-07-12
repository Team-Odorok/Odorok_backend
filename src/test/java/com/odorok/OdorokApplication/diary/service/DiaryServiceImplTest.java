package com.odorok.OdorokApplication.diary.service;

import com.odorok.OdorokApplication.commons.exception.GptCommunicationException;
import com.odorok.OdorokApplication.commons.exception.NotFoundException;
import com.odorok.OdorokApplication.diary.dto.request.DiaryRegenerationRequest;
import com.odorok.OdorokApplication.diary.dto.response.*;
import com.odorok.OdorokApplication.diary.repository.VisitedCourseRepository;
import com.odorok.OdorokApplication.diary.dto.gpt.VisitedAdditionalAttraction;
import com.odorok.OdorokApplication.diary.dto.gpt.VisitedCourseAndAttraction;
import com.odorok.OdorokApplication.diary.dto.request.DiaryChatAnswerRequest;
import com.odorok.OdorokApplication.diary.repository.DiaryRepository;
import com.odorok.OdorokApplication.diary.repository.VisitedCourseRepository;
import com.odorok.OdorokApplication.draftDomain.Inventory;
import com.odorok.OdorokApplication.gpt.service.GptService;
import com.odorok.OdorokApplication.diary.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.Nested;


import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Mock
    private VisitedCourseRepository visitedCourseRepository;

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

    @Test
    void 방문_완료_및_일지_생성_이전인_방문코스_목록_조회_성공() {
        long userId = 1L;
        List<VisitedCourseSummary> mockList = List.of(
                new VisitedCourseSummary(1L, LocalDateTime.of(2024, 1, 1, 10, 0), "Course A"),
                new VisitedCourseSummary(2L, LocalDateTime.of(2024, 2, 1, 10, 0), "Course B")
        );
        when(visitedCourseRepository.findVisitedCourseWithoutDiaryByUserId(userId))
                .thenReturn(mockList);

        VisitedCourseWithoutDiaryResponse response = diaryService.findVisitedCourseWithoutDiaryByUserId(userId);

        assertNotNull(response);
        assertEquals(2, response.getResponse().size());
        assertEquals("Course A", response.getResponse().get(0).getCourseName());

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
            assertEquals("오늘 여행은 어땠나요?", response.getContent());
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

    @Nested
    class InsertAnswerTest{
        @Mock
        private GptService gptService;

        @InjectMocks
        private DiaryServiceImpl diaryService;

        private final long userId = 1L;

        private final String rawPromptTemplate =
                "당신은 여행 기록 작가입니다. 여행자의 스타일은 {style}, 코스명은 {courseName}, 설명은 {courseSummary}, 주변 명소는 {additionalAttractions}입니다.";
        private List<GptService.Prompt> chatLog;
        private String newAnswer;
        private List<GptService.Prompt> returnChatLog;

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(diaryService, "rawSystemPrompt",rawPromptTemplate);
            
            // GPT 응답 mock
            chatLog = List.of(
                    new GptService.Prompt("system", "시스템 프롬프트"),
                    new GptService.Prompt("assistant", "오늘 여행은 어땠나요?")
            );

            newAnswer = "날씨가 좋아서 걷기 좋았어";
            GptService.Prompt prompt= new GptService.Prompt("user", newAnswer);
            GptService.Prompt answerPrompt = new GptService.Prompt("assistant", "날씨가 좋았군요. 그 때 감정은 어땠나요?");
            returnChatLog = new ArrayList<>(chatLog);
            returnChatLog.add(prompt);
            returnChatLog.add(answerPrompt);
        }

        @Test
        void 사용자_답변_후_gpt_호출_성공() {
            // given
            when(gptService.sendPrompt(anyList(), any(GptService.Prompt.class))).thenReturn(returnChatLog);

            // when
            DiaryChatResponse response = diaryService.insertAnswer(userId, new DiaryChatAnswerRequest(newAnswer, chatLog));

            // then
            assertNotNull(response);
            List<GptService.Prompt> responseChatLog = response.getChatLog();
            assertEquals(responseChatLog.get(responseChatLog.size() - 1).getRole(), "assistant");
            assertEquals(responseChatLog.get(responseChatLog.size() - 2).getRole(), "user");
            verify(gptService, times(1)).sendPrompt(anyList(), any(GptService.Prompt.class));
        }

        @Test
        void 사용자_답변_후_gpt_호출_응답없음_예외발생() {
            // given
            when(gptService.sendPrompt(anyList(), any(GptService.Prompt.class))).thenReturn(List.of());

            // when & then
            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    diaryService.insertAnswer(userId, new DiaryChatAnswerRequest(newAnswer, chatLog)));

            assertEquals("GPT 응답이 비어있음 ", ex.getMessage());
        }
    }

    @Nested
    class InsertRegenerationTest{
        @Mock
        private GptService gptService;

        @InjectMocks
        private DiaryServiceImpl diaryService;

        private final long userId = 1L;

        private final String regenerationPrompt = """
                완성된 일지가 마음에 들지 않습니다. 사실과 순서는 그대로 두고, 전반적인 문장 흐름/가독성/톤을 자연스럽게 다듬어 다시 작성해주세요.\s
                    일지 내용에 지금 내용을 포함하지 않고, 이전 내용을 참고해 작성해주세요.\s
                    구체적인 피드백을 참고해 다시 작성해주세요. 다음과 같습니다. `{feedback}`
                    일지는 마찬가지로 마무리 멘트 없이 바로 일기만 출력합니다. 마지막에 반드시 `<END>`를 붙입니다
                """;
        private final String defaultFeedbackPrompt = """
                앞서 말했듯 사실과 순서는 그대로 두고, 문장 흐름과 가독성을 자연스럽게 다듬어 다시 작성해주세요.
                    주요 감정(느낀 점)을 강조해주고, 일지를 읽었을 때 사용자가 그 때의 감정과 기분을 생생하게 느낄 수 있도록 작성해주세요.
                    중복 표현을 되도록 줄여주세요. 핵심 정보는 유지하면서 전체적인 스타일은 이전에 요청한대로 반영해서 작성해주세요.\s
                """;

        private List<GptService.Prompt> chatLog;
        private String feedback;
        private List<GptService.Prompt> returnChatLog;

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(diaryService, "regenerationPrompt",regenerationPrompt);
            ReflectionTestUtils.setField(diaryService, "defaultFeedbackPrompt",defaultFeedbackPrompt);

            feedback = "일지 말투가 너무 딱딱해요. 조금 더 자연스럽게 해주세요.";
        }

        @Test
        void 일지_재생성_사용자_피드백_있는_경우_요청_성공() {
            // given
            GptService.Prompt prompt = diaryService.buildRegenerationPrompt(feedback);
            chatLog = List.of(prompt);
            GptService.Prompt responsePrompt = new GptService.Prompt("assistant", "재생성된 일지입니다. <END>");
            returnChatLog = new ArrayList<>(chatLog);
            returnChatLog.add(responsePrompt);
            when(gptService.sendPrompt(anyList(), any(GptService.Prompt.class))).thenReturn(returnChatLog);

            // when
            DiaryChatResponse response = diaryService.insertRegeneration(userId, new DiaryRegenerationRequest(feedback, chatLog));

            List<GptService.Prompt> responseChatLog = response.getChatLog();
            GptService.Prompt userResponsePrompt = responseChatLog.get(responseChatLog.size() - 2);
            GptService.Prompt assistantResponsePrompt = responseChatLog.get(responseChatLog.size() - 1);

            // then
            assertNotNull(response);
            assertEquals(assistantResponsePrompt.getRole(), "assistant");
            assertEquals(userResponsePrompt.getRole(), "user");
            assertTrue(userResponsePrompt.getContent().contains(feedback));
            verify(gptService, times(1)).sendPrompt(anyList(), any(GptService.Prompt.class));
        }

        @Test
        void 일지_재생성_사용자_피드백_없는_경우_요청_성공() {
            // given
            feedback = null;
            GptService.Prompt prompt = diaryService.buildRegenerationPrompt(defaultFeedbackPrompt);
            chatLog = List.of(prompt);
            GptService.Prompt responsePrompt = new GptService.Prompt("assistant", "재생성된 일지입니다. <END>");
            returnChatLog = new ArrayList<>(chatLog);
            returnChatLog.add(responsePrompt);
            when(gptService.sendPrompt(anyList(), any(GptService.Prompt.class))).thenReturn(returnChatLog);

            // when
            DiaryChatResponse response = diaryService.insertRegeneration(userId, new DiaryRegenerationRequest(null, chatLog));

            List<GptService.Prompt> responseChatLog = response.getChatLog();
            GptService.Prompt userResponsePrompt = responseChatLog.get(responseChatLog.size() - 2);
            GptService.Prompt assistantResponsePrompt = responseChatLog.get(responseChatLog.size() - 1);

            // then
            assertNotNull(response);
            assertEquals(assistantResponsePrompt.getRole(), "assistant");
            assertEquals(userResponsePrompt.getRole(), "user");
            assertTrue(userResponsePrompt.getContent().contains(defaultFeedbackPrompt));
            verify(gptService, times(1)).sendPrompt(anyList(), any(GptService.Prompt.class));
        }
        @Test
        void 일지_재생성_시_GPT_응답에_END_누락() {
            // given
            chatLog = List.of(diaryService.buildRegenerationPrompt(feedback));
            GptService.Prompt responsePrompt = new GptService.Prompt("assistant", "재생성된 일지입니다.");
            returnChatLog = new ArrayList<>(chatLog);
            returnChatLog.add(responsePrompt);
            when(gptService.sendPrompt(anyList(), any(GptService.Prompt.class))).thenReturn(returnChatLog);

            GptCommunicationException ex = assertThrows(GptCommunicationException.class, () ->
                    diaryService.insertRegeneration(userId, new DiaryRegenerationRequest(feedback, chatLog)));

            assertEquals("GPT 응답에 <END> 토큰이 누락", ex.getMessage());
        }

        @Test
        void 사용자_답변_후_gpt_호출_응답없음_예외발생() {
            // given
            chatLog = List.of(diaryService.buildRegenerationPrompt(feedback));
            when(gptService.sendPrompt(anyList(), any(GptService.Prompt.class))).thenReturn(List.of());

            // when & then
            GptCommunicationException ex = assertThrows(GptCommunicationException.class, () ->
                    diaryService.insertRegeneration(userId, new DiaryRegenerationRequest(feedback, chatLog)));

            assertEquals("GPT 응답이 비어있음 ", ex.getMessage());
        }
    }

}
