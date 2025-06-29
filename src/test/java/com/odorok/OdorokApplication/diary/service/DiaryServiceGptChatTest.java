package com.odorok.OdorokApplication.diary.service;

import com.odorok.OdorokApplication.diary.dto.gpt.VisitedAdditionalAttraction;
import com.odorok.OdorokApplication.diary.dto.gpt.VisitedCourseAndAttraction;
import com.odorok.OdorokApplication.gpt.service.GptService;
import com.odorok.OdorokApplication.diary.repository.VisitedCourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

//@SpringBootTest()
public class DiaryServiceGptChatTest {

//    @MockBean
//    private VisitedCourseRepository visitedCourseRepository;
//
//    @Autowired
//    private DiaryServiceImpl diaryService;
//
//    @Value("${gpt.system-prompt}")
//    private String rawSystemPrompt;
//
//    private final long userId = 1L;
//    private final long visitedCourseId = 123L;
//    private final String style = "감성적인";
//
//    @BeforeEach
//    void setUp() {
//        VisitedAdditionalAttraction mockAttraction1 = new VisitedAdditionalAttraction("해운대", "부산 해운대구 우동", "해운대해수욕장, 광안리해수욕장, 벡스코(BEXCO), 누리마루 등이 있다.");
//        VisitedAdditionalAttraction mockAttraction2 = new VisitedAdditionalAttraction("해운대블루라인파크", "부산광역시 해운대구 달맞이길62번길 13","수려한 해안 절경을 따라 해운대 해변열차와 해운대 스카이캡슐을 운행");
//        VisitedCourseAndAttraction mockVisited = VisitedCourseAndAttraction.builder()
//                .courseName("남파랑길 1코스")
//                .courseSummary("- 해파랑길 시종점인 오륙도 해맞이 공원에서부터 부산 중구 부산대교까지 이어지는 구간<br>- 신선이 노닐던 신선대 및 부산항의 역동적인 파노라마를 만끽할 수 있는 구간으로 세계에서 하나뿐인 UN기념공원 및 부산박물관, 영화 “친구”로 유명한 부산 일대의 명소를 함께 체험할 수 있는 코스<br>- 아름다운 해안경관과 우리나라 제1의 항구도시 부산의 매력을 느낄 수 있는 구간<br>- 부산 갈맷길 3-1, 3-2코스가 중첩됨")
//                .visitedAttractions(List.of(mockAttraction1, mockAttraction2))
//                .build();
//
//        when(visitedCourseRepository.findCourseAndAttractionsByVisitedCourseId(userId, visitedCourseId))
//                .thenReturn(mockVisited);
//    }
//
//    // GPT 외부 API 호출 테스트 코드
//
////    @Test
////    void 일지_생성_시작_실제GPT호출_성공() {
////        // when
////        DiaryChatResponse response = diaryService.insertGeneration(userId, style,visitedCourseId);
////
////        assertNotNull(response);
////        System.out.println(response);
////        assertEquals(response.getChatLog().get(0).getRole(), "system");
////        assertEquals(response.getChatLog().get(1).getRole(), "assistant");
////    }
//
//    @Test
//    void 시스템_프롬프트_정상생성_성공() {
//        // given
//        long userId = 1L;
//        long visitedCourseId = 123L;
//        String style = "감성적인";
//
//        // when
//        GptService.Prompt prompt = diaryService.buildFinalSystemPrompt(userId, style, visitedCourseId);
//
//        // then
//        assertNotNull(prompt);
//        assertEquals("system", prompt.getRole());
////        assertTrue(prompt.getContent().contains("남파랑길 1코스"));
////        assertTrue(prompt.getContent().contains("감성적인"));
////        assertTrue(prompt.getContent().contains("해운대"));
////        assertTrue(prompt.getContent().contains("해운대블루라인파크"));
//    }
}
