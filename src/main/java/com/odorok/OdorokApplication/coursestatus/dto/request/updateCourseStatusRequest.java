package com.odorok.OdorokApplication.coursestatus.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class updateCourseStatusRequest {
    private Double latitude;
    private Double longitude;
}
