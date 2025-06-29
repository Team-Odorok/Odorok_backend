package com.odorok.OdorokApplication.diary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class DiaryPermissionCheckResponse {
    private Long userId;
    private Long itemId;
    private int count;
    private boolean canCreateDiary;

    public DiaryPermissionCheckResponse(Long userId, Long itemId, int count) {
        this.userId = userId;
        this.itemId = itemId;
        this.count = count;
        this.canCreateDiary = count > 0;
    }

}
