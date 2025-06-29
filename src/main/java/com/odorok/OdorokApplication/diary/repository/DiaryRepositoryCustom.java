package com.odorok.OdorokApplication.diary.repository;

import com.odorok.OdorokApplication.diary.dto.response.DiaryDetail;

public interface DiaryRepositoryCustom {
    public DiaryDetail findDiaryById(long userId, long diaryId);
}