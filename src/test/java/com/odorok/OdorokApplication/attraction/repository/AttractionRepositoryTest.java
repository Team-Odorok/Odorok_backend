package com.odorok.OdorokApplication.attraction.repository;

import com.odorok.OdorokApplication.attraction.config.AttractionTestDataConfiguration;
import com.odorok.OdorokApplication.commons.querydsl.config.QueryDslConfig;
import com.odorok.OdorokApplication.draftDomain.Attraction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Import({QueryDslConfig.class, AttractionTestDataConfiguration.class})
@ActiveProfiles("test")
@Sql("/sql/attraction-test-data.sql")
class AttractionRepositoryTest {
    @Autowired
    private AttractionRepository attractionRepository;

    @Autowired
    private Map<String, Integer> contentTypeMap;

    private final Integer SEOUL_SIDO_CODE = 1;
    private final Integer SEOUL_GANNAMGU_SIGUNGU_CODE = 1;

    @Test
    public void 시도_시군구_코드_컨텐츠타입아이디로_명소조회에_성공한다() {
        List<Attraction> result = attractionRepository.findBySidoCodeAndSigunguCodeAndContentTypeId(
                1, 20, 14
        );

        assertThat(result.size()).isNotZero();
    }
}