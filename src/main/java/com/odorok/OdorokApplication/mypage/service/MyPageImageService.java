package com.odorok.OdorokApplication.mypage.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MyPageImageService {
    public List<String> insertProfileImage(Long userId, List<MultipartFile> images);
    public void deleteImages(List<String> urls);
}
