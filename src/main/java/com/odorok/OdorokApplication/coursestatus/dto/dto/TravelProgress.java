package com.odorok.OdorokApplication.coursestatus.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class TravelProgress {
    private long traveledMeters;
    private long lastPassedIndex;

}
