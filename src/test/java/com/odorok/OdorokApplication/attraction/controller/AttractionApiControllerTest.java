package com.odorok.OdorokApplication.attraction.controller;

import com.odorok.OdorokApplication.attraction.config.AttractionTestDataConfiguration;
import com.odorok.OdorokApplication.attraction.dto.response.item.AttractionDetail;
import com.odorok.OdorokApplication.attraction.dto.response.item.AttractionSummary;
import com.odorok.OdorokApplication.attraction.dto.response.item.ContentTypeSummary;
import com.odorok.OdorokApplication.attraction.service.AttractionQueryService;
import com.odorok.OdorokApplication.security.filter.KakaoLoginFilter;
import com.odorok.OdorokApplication.security.service.UserQueryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@WebMvcTest(controllers = {AttractionApiController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(AttractionTestDataConfiguration.class)
@WithMockUser(username = "test", roles = "USER")
class AttractionApiControllerTest {
//    @MockitoBean
//    private JWTAuthenticationFilter jwtAuthenticationFilter;
//    @MockitoBean
//    private JWTVerificationFilter jwtVerificationFilter;
    @MockitoBean
    private AttractionQueryService attractionQueryService;
    @MockitoBean
    private KakaoLoginFilter kakaoLoginFilter;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Map<String, Integer> contentTypeMap;

    private final Integer SEOUL_SIDO_CODE = 1;
    private final Integer SEOUL_GANNAMGU_SIGUNGU_CODE = 1;
    private final String DUMMY_OVERVIEW = "test 개요";


    private final String URI = "http://localhost";
    private final int LOCAL_PORT = 8080;
    private final String COMMON_REQUEST_PATH = "/api/attractions";
    private final String CONTENTTYPE_PATH = "/contenttypes";
    private final String REGIONAL_ATTR_PATH = "/region";
    private final String ATTR_DETAIL_PATH = "/detail";

    @Test
    public void 컨텐츠타입_리스트_조회에_성공한다() throws Exception{
        String url = UriComponentsBuilder.fromUriString(URI).port(LOCAL_PORT)
                .path(COMMON_REQUEST_PATH + CONTENTTYPE_PATH).toUriString();

        Mockito.when(attractionQueryService.queryAllContentTypes()).thenReturn(
                List.of(new ContentTypeSummary(1, "문화시설"),
                        new ContentTypeSummary(2, "운동시설"),
                        new ContentTypeSummary(3, "음식점")
                        )
        );

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(url));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andDo(MockMvcResultHandlers.print());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.data.items[0].name").value("문화시설"));
    }

    @Test
    public void 행정동_코드와_컨텐츠_타입으로_명소_검색에_성공한다() throws Exception {
        String url = UriComponentsBuilder.fromUriString(URI).port(LOCAL_PORT)
                .path(COMMON_REQUEST_PATH + REGIONAL_ATTR_PATH)
                .queryParam("sidoCode",SEOUL_SIDO_CODE)
                .queryParam("sigunguCode", SEOUL_GANNAMGU_SIGUNGU_CODE)
                .queryParam("contentTypeId", contentTypeMap.get("쇼핑")).toUriString();

        Mockito.when(attractionQueryService.queryRegionalAttractions(
                SEOUL_SIDO_CODE, SEOUL_GANNAMGU_SIGUNGU_CODE, contentTypeMap.get("쇼핑")
        )).thenReturn(List.of(
                new AttractionSummary(), new AttractionSummary(), new AttractionSummary()
        ));

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(url));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.data.items").isNotEmpty());
        Mockito.verify(attractionQueryService, Mockito.times(1)).queryRegionalAttractions(
                SEOUL_SIDO_CODE, SEOUL_GANNAMGU_SIGUNGU_CODE, contentTypeMap.get("쇼핑")
        );
    }

    @Test
    public void 명소_상세_조회에_성공한다() throws Exception{
        // given
        String url = UriComponentsBuilder.fromUriString(URI).port(LOCAL_PORT)
                .path(COMMON_REQUEST_PATH + ATTR_DETAIL_PATH)
                .queryParam("attractionId", "1").toUriString();
        Mockito.when(attractionQueryService.queryAttractionDetail(1L)).thenReturn(
                new AttractionDetail(DUMMY_OVERVIEW));

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(url));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.data.overview").value(DUMMY_OVERVIEW));
        Mockito.verify(attractionQueryService, Mockito.times(1)).queryAttractionDetail(1L);
    }

    @Test
    public void 없는_명소를_요청한다() throws Exception{
// given
        String url = UriComponentsBuilder.fromUriString(URI).port(LOCAL_PORT)
                .path(COMMON_REQUEST_PATH + ATTR_DETAIL_PATH)
                .queryParam("attractionId", "1").toUriString();
        Mockito.when(attractionQueryService.queryAttractionDetail(1L)).thenThrow(
                new IllegalArgumentException("없는 '명소' 식별자 = " + 1L));

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(url));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isBadRequest());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.data").doesNotExist());
    }
}