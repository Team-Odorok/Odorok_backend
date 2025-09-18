package com.odorok.OdorokApplication.diary.repository;

import com.odorok.OdorokApplication.domain.DiaryImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiaryImageRepository extends JpaRepository<DiaryImage, Long> {
    List<DiaryImage> findByDiaryId(Long diaryId);
    void deleteAllByDiaryId(Long diaryId);
}
