package com.odorok.OdorokApplication.region.repository;

import com.odorok.OdorokApplication.commons.querydsl.config.QueryDslConfig;
import com.odorok.OdorokApplication.infrastructures.domain.Sido;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DataJpaTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
class SidoRepositoryTest {

    @Autowired
    private SidoRepository sidoRepository;

    @BeforeEach
    void setUp() {
        List<Sido> sidos = Arrays.asList(
                new Sido(1, "서울특별시"),
                new Sido(2, "인천광역시"),
                new Sido(3, "대전광역시"),
                new Sido(4, "대구광역시"),
                new Sido(5, "광주광역시"),
                new Sido(6, "부산광역시"),
                new Sido(7, "울산광역시"),
                new Sido(8, "세종특별자치시"),
                new Sido(31, "경기도"),
                new Sido(32, "강원특별자치도"),
                new Sido(33, "충청북도"),
                new Sido(34, "충청남도"),
                new Sido(35, "경상북도"),
                new Sido(36, "경상남도"),
                new Sido(37, "전북특별자치도"),
                new Sido(38, "전라남도"),
                new Sido(39, "제주특별자치도")
        );
        sidoRepository.saveAll(sidos);
    }

    @AfterEach
    void tearDown() {
        sidoRepository.deleteAll();
    }

    @Test
    public void 시도_조회에_성공한다() {
        List<Sido> result = sidoRepository.findAll();
        assertNotEquals(0, result.size());
    }
}
