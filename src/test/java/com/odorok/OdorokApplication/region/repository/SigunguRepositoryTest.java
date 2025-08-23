package com.odorok.OdorokApplication.region.repository;

import com.odorok.OdorokApplication.commons.querydsl.config.QueryDslConfig;
import com.odorok.OdorokApplication.infrastructures.domain.Sigungu;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
class SigunguRepositoryTest {
    private final int SEOUL_SIDO_CODE = 1;
    private final int NO_SIDO_CODE = 100;

    @Autowired
    private SigunguRepository sigunguRepository;

    @Test
    public void 시군구_조회에_성공한다() {
        List<Sigungu> result = sigunguRepository.findBySidoCode(SEOUL_SIDO_CODE);

        assertThat(result.size()).isNotZero();
    }

    @Test
    public void 없는_시도의_행정동_조회에서_빈_리스트가_출력된다() {
        List<Sigungu> result = sigunguRepository.findBySidoCode(NO_SIDO_CODE);

        assertThat(result.size()).isZero();
    }
}