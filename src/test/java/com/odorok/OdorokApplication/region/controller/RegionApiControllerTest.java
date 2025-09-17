package com.odorok.OdorokApplication.region.controller;

import com.odorok.OdorokApplication.course.service.CourseQueryService;
import com.odorok.OdorokApplication.region.dto.response.item.SidoSummary;
import com.odorok.OdorokApplication.region.dto.response.item.SigunguSummary;
import com.odorok.OdorokApplication.region.service.RegionQueryService;
import com.odorok.OdorokApplication.security.filter.KakaoLoginFilter;
import com.odorok.OdorokApplication.security.service.UserQueryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(controllers = {RegionApiController.class})
@AutoConfigureMockMvc(addFilters = false)
class RegionApiControllerTest {
    private final String URI = "http://localhost";
    private final int LOCAL_PORT = 8080;
    private final String COMMON_REQUEST_PATH = "/api/regions";
    private final String SIDO_PATH = "/sido";
    private final String SIGUNGU_PATH = "/sigungu";

    private static final int SEOUL_SIDO_CODE = 1;
    private static final int BUSAN_SIDO_CODE = 6;
//    @MockitoBean
//    private JWTAuthenticationFilter jwtAuthenticationFilter;
//    @MockitoBean
//    private JWTVerificationFilter jwtVerificationFilter;

    @MockitoBean
    private RegionQueryService regionQueryService;
    @MockitoBean
    private CourseQueryService courseQueryService;
    @MockitoBean
    private KakaoLoginFilter kakaoLoginFilter;


    @Autowired
    private MockMvc mockMvc;

    @Test
    public void 시도_목록을_조회하는데_성공한다() throws Exception {
        String url = UriComponentsBuilder.fromUriString(URI)
                .port(LOCAL_PORT)
                .path(COMMON_REQUEST_PATH+SIDO_PATH).toUriString();
        Mockito.when(courseQueryService.queryValidSidoCodes()).thenReturn(Set.of(BUSAN_SIDO_CODE));
        Mockito.when(regionQueryService.queryAllSidos()).thenReturn(List.of(
                new SidoSummary("서울특별시", SEOUL_SIDO_CODE), new SidoSummary("부산광역시", BUSAN_SIDO_CODE)
        ));

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(url));
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.items[0].sidoCode").value(BUSAN_SIDO_CODE))
                .andDo(print());
    }

    @Test
    public void 특정시도_행정동_조회에_실패한다() throws Exception {
        String url = UriComponentsBuilder.fromUriString(URI)
                .port(LOCAL_PORT)
                .path(COMMON_REQUEST_PATH+SIGUNGU_PATH)
                .queryParam("sidoCode", SEOUL_SIDO_CODE)
                .toUriString();
        
        Mockito.when(courseQueryService.queryValidSidoCodes()).thenReturn(Set.of(BUSAN_SIDO_CODE));

        Mockito.when(regionQueryService.queryAllSigunguOf(SEOUL_SIDO_CODE)).thenThrow(new RuntimeException("호출되면 안됨."));
        
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(url));
        
        resultActions.andExpect(MockMvcResultMatchers.status().isBadRequest());
        resultActions.andDo(MockMvcResultHandlers.print());

        Mockito.verify(regionQueryService, Mockito.never()).queryAllSigunguOf(SEOUL_SIDO_CODE);
    }

    @Test
    public void 특정시도_행정동_조회에_성공한다() throws Exception {
        String url = UriComponentsBuilder.fromUriString(URI)
                .port(LOCAL_PORT)
                .path(COMMON_REQUEST_PATH+SIGUNGU_PATH)
                .queryParam("sidoCode", BUSAN_SIDO_CODE)
                .toUriString();

        Mockito.when(courseQueryService.queryValidSidoCodes()).thenReturn(Set.of(BUSAN_SIDO_CODE));

        Mockito.when(regionQueryService.queryAllSigunguOf(BUSAN_SIDO_CODE)).thenReturn(
                List.of(new SigunguSummary("중구", 1), new SigunguSummary("광진구", 2),
                        new SigunguSummary("노원구", 3), new SigunguSummary("마포구", 4)));

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(url));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andDo(MockMvcResultHandlers.print());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.data.items[0].name").value("중구"));
    }
}