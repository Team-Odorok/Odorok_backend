package com.odorok.OdorokApplication.mypage.dto.response;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Getter
@Setter
public class UserHealthInfoResponse {

    private Boolean gender;

    private Double height;

    private Double weight;

    private Integer age;

    private Boolean smoking;

    private Integer drinkPerWeek;

    private Integer exercisePerWeek;

    private List<Long> diseaseList;

}
