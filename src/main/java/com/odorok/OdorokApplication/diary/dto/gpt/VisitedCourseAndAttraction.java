package com.odorok.OdorokApplication.diary.dto.gpt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@Builder
public class VisitedCourseAndAttraction {
    private String courseName;
    private String courseSummary;
    @Setter
    private List<VisitedAdditionalAttraction> visitedAttractions;

    public String attractionsToString() {
        if(visitedAttractions.size() == 0) return "";
        String attractions = visitedAttractions.stream()
                .map(VisitedAdditionalAttraction::getTitle) // 명소 이름만 추출
                .distinct()
                .collect(Collectors.joining(", "));

        return String.format("코스 외에도 사용자는 %s에 다녀왔습니다.", attractions);
    }
}