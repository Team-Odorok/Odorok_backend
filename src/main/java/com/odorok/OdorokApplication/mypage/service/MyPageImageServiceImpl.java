package com.odorok.OdorokApplication.mypage.service;

import com.odorok.OdorokApplication.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageImageServiceImpl implements MyPageImageService {
    private final S3Service s3Service;

    @Override
    public List<String> insertProfileImage(Long userId, List<MultipartFile> images) {
        return s3Service.uploadMany("mypage", String.valueOf(userId),images);
    }

    @Override
    public void deleteImages(List<String> urls) {
        s3Service.deleteMany(urls);
    }
}
