package com.odorok.OdorokApplication.diary.repository;

import com.odorok.OdorokApplication.domain.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryRepository extends JpaRepository<Diary,Long>, DiaryRepositoryCustom {
}
