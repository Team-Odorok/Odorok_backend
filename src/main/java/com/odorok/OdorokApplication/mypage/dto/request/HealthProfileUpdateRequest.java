package com.odorok.OdorokApplication.mypage.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class HealthProfileUpdateRequest {
    private Boolean gender;
    private Double height;
    private Double weight;
    private Integer age;
    private Boolean smoking;
    private Integer drinkPerWeek;
    private Integer exercisePerWeek;
}
