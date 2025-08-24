package com.odorok.OdorokApplication.attraction.integration;

import com.odorok.OdorokApplication.attraction.config.AttractionTestDataConfiguration;
import com.odorok.OdorokApplication.attraction.dto.response.item.AttractionSummary;
import com.odorok.OdorokApplication.attraction.dto.response.item.ContentTypeSummary;
import com.odorok.OdorokApplication.attraction.repository.AttractionRepository;
import com.odorok.OdorokApplication.attraction.repository.ContentTypeRepository;
import com.odorok.OdorokApplication.commons.response.ResponseRoot;
import com.odorok.OdorokApplication.draftDomain.Attraction;
import com.odorok.OdorokApplication.draftDomain.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(AttractionTestDataConfiguration.class)
@ActiveProfiles("test")
public class AttractionIntegrationTest {
    @LocalServerPort
    private Integer LOCAL_PORT;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AttractionRepository attractionRepository;

    @Autowired
    private ContentTypeRepository contentTypeRepository;

    private final String COMMON_URL = "http://localhost";
    private final String CONTENTTYPE_PATH = "/api/attractions/contenttypes";
    private final String REGIONAL_ATTRACTION_PATH = "/api/attractions/region";
    private final String ATTRACTION_DETAIL_PATH = "/api/attractions/detail";

    @BeforeEach
    void setUp() {
        contentTypeRepository.deleteAll();
        attractionRepository.deleteAll();

        List<ContentType> contentTypes = Arrays.asList(
            ContentType.builder().name("관광지").build(),
            ContentType.builder().name("문화시설").build(),
            ContentType.builder().name("축제공연행사").build(),
            ContentType.builder().name("여행코스").build(),
            ContentType.builder().name("레포츠").build(),
            ContentType.builder().name("숙박").build(),
            ContentType.builder().name("쇼핑").build(),
            ContentType.builder().name("음식점").build()
        );
        contentTypeRepository.saveAll(contentTypes);

        Map<String, Integer> contentTypeNameToIdMap = contentTypeRepository.findAll().stream()
                .collect(Collectors.toMap(ContentType::getName, ContentType::getId));

        Attraction attractionForDetail = Attraction.builder()
                .id(125266L)
                .contentId(125266)
                .title("국립중앙박물관")
                .firstImage1("http://tong.visitkorea.or.kr/cms/resource/84/2686884_image2_1.jpg")
                .firstImage2("http://tong.visitkorea.or.kr/cms/resource/84/2686884_image3_1.jpg")
                .mapLevel(6)
                .latitude(37.5238)
                .longitude(126.9805)
                .tel("02-2077-9000")
                .addr1("서울특별시 용산구 서빙고로 137")
                .addr2("")
                .homepage("<a href=\"http://www.museum.go.kr\" target=\"_blank\" title=\"새창 : 국립중앙박물관 홈페이지로 이동\">www.museum.go.kr</a>")
                .overview("대한민국 역사와 문화의 정수를 간직한 곳")
                .contentTypeId(contentTypeNameToIdMap.get("문화시설"))
                .sidoCode(1)
                .sigunguCode(20)
                .build();

        Attraction attractionForSearch = Attraction.builder()
                .id(12345L)
                .contentId(12345)
                .title("테스트 쇼핑 센터")
                .firstImage1("")
                .firstImage2("")
                .mapLevel(6)
                .latitude(37.5665)
                .longitude(126.9780)
                .tel("02-1234-5678")
                .addr1("서울특별시 중구")
                .addr2("")
                .homepage("")
                .overview("테스트용 쇼핑 센터입니다.")
                .contentTypeId(contentTypeNameToIdMap.get("쇼핑"))
                .sidoCode(1)
                .sigunguCode(1)
                .build();
        
        attractionRepository.saveAll(Arrays.asList(attractionForDetail, attractionForSearch));
    }

    @AfterEach
    void tearDown() {
        attractionRepository.deleteAll();
        contentTypeRepository.deleteAll();
    }

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
        Integer shoppingContentTypeId = contentTypeRepository.findAll().stream()
                .filter(contentType -> "쇼핑".equals(contentType.getName()))
                .findFirst()
                .map(ContentType::getId)
                .orElseThrow(() -> new IllegalStateException("Shopping content type not found"));

        String url = UriComponentsBuilder.fromUriString(COMMON_URL).port(LOCAL_PORT)
                .path(REGIONAL_ATTRACTION_PATH)
                .queryParam("sidoCode", "1")
                .queryParam("sigunguCode", "1")
                .queryParam("contentTypeId", shoppingContentTypeId)
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
