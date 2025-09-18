package com.odorok.OdorokApplication.diary.service;

import com.odorok.OdorokApplication.diary.repository.DiaryImageRepository;
import com.odorok.OdorokApplication.diary.repository.DiaryRepository;
import com.odorok.OdorokApplication.domain.DiaryImage;
import com.odorok.OdorokApplication.s3.service.S3Service;
import com.odorok.OdorokApplication.s3.service.S3ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RequiredArgsConstructor
@Service
@Slf4j
public class DiaryImageServiceImpl implements DiaryImageService {
    private final S3Service s3Service;
    private final DiaryImageRepository diaryImageRepository;
    private final String DOMAIN = "diary";

    @Override
    public List<String> insertDiaryImage(List<MultipartFile> images, Long userId) {
        return s3Service.uploadMany(DOMAIN, String.valueOf(userId), images);
    }

    @Override
    public void insertDiaryImageUrl(List<String> imgUrls, Long diaryId) {
        for(String imgUrl : imgUrls) {
            DiaryImage diaryImage = DiaryImage.builder()
                    .imgUrl(imgUrl)
                    .diaryId(diaryId)
                    .build();
            diaryImageRepository.save(diaryImage);
        }
    }

//    @Override
//    public void deleteDiaryImages(List<String> imgUrls) {
//        s3Service.deleteMany(imgUrls);
//    }

    @Override
    public List<DiaryImage> getDiaryImages(Long diaryId) {
        return diaryImageRepository.findByDiaryId(diaryId);
    }
}
