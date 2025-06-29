package com.odorok.OdorokApplication.diary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class DiaryDetail {
    private Long id;
    private String title;
    private String content;
    @Setter
    private List<String> imgs;
    private Long userId;
    private String courseName;
    private LocalDateTime visitedAt;
    private LocalDateTime createdAt;

    public DiaryDetail(Long id, String title, String content, Long userId, String courseName, LocalDateTime visitedAt, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.courseName = courseName;
        this.visitedAt = visitedAt;
        this.createdAt = createdAt;
    }
}
