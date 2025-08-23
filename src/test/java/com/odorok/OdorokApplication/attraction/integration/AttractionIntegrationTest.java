package com.odorok.OdorokApplication.attraction.integration;

import com.odorok.OdorokApplication.attraction.config.AttractionTestDataConfiguration;
import com.odorok.OdorokApplication.attraction.dto.response.item.AttractionDetail;
import com.odorok.OdorokApplication.attraction.dto.response.item.AttractionSummary;
import com.odorok.OdorokApplication.attraction.dto.response.item.ContentTypeSummary;
import com.odorok.OdorokApplication.commons.response.ResponseRoot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(AttractionTestDataConfiguration.class)
@ActiveProfiles("test")
@Sql("/sql/attraction-test-data.sql")
public class AttractionIntegrationTest {
    @LocalServerPort
    private Integer LOCAL_PORT;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Map<String, Integer> contentTypeMap;

    private final String COMMON_URL = "http://localhost";
    private final String CONTENTTYPE_PATH = "/api/attractions/contenttypes";
    private final String REGIONAL_ATTRACTION_PATH = "/api/attractions/region";
    private final String ATTRACTION_DETAIL_PATH = "/api/attractions/detail";

    //Test#1 : 컨텐츠 타입 조회
    @Test
    public void 컨텐츠타입_목록_조회에_성공한다() {
        String url = UriComponentsBuilder.fromUriString(COMMON_URL).port(LOCAL_PORT).path(CONTENTTYPE_PATH).toUriString();

        ResponseRoot root = restTemplate.getForObject(url, ResponseRoot.class);
        List<ContentTypeSummary> contentTypes = (List<ContentTypeSummary>)((LinkedHashMap)root.getData()).get("items");

        System.out.println(contentTypes);
        assertThat(contentTypes).isNotNull();
        assertThat(contentTypes.size()).isNotZero();
    }
    //Test#2 : 지역 코드로 명소 조회
    @Test
    public void 지역_코드로_명소_조회에_성공한다() {
        String url = UriComponentsBuilder.fromUriString(COMMON_URL).port(LOCAL_PORT)
                .path(REGIONAL_ATTRACTION_PATH)
                .queryParam("sidoCode", "1")
                .queryParam("sigunguCode", "1")
                .queryParam("contentTypeId", contentTypeMap.get("쇼핑"))
                .toUriString();

        ResponseRoot root = restTemplate.getForObject(url, ResponseRoot.class);
        List<AttractionSummary> attractions = (List<AttractionSummary>)((LinkedHashMap)root.getData()).get("items");

        System.out.println(attractions);
        assertThat(attractions).isNotNull();
        assertThat(attractions.size()).isNotZero();
    }

    //Test#3 : 명소 상세 조회
    @Test
    public void 명소_상세_조회에_성공한다() {
        String url = UriComponentsBuilder.fromUriString(COMMON_URL).port(LOCAL_PORT)
                .path(ATTRACTION_DETAIL_PATH)
                .queryParam("attractionId", "125266")
                .toUriString();

        ResponseRoot root = restTemplate.getForObject(url, ResponseRoot.class);
        String overview = (String)((LinkedHashMap)root.getData()).get("overview");

        System.out.println(overview);
        assertThat(overview).isNotNull();
    }

    @Test
    public void 명소_상세_조회에_실패한다() {
        String url = UriComponentsBuilder.fromUriString(COMMON_URL).port(LOCAL_PORT)
                .path(ATTRACTION_DETAIL_PATH)
                .queryParam("attractionId", "-1")
                .toUriString();

        ResponseEntity<ResponseRoot> response = restTemplate.getForEntity(url, ResponseRoot.class);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }
}
