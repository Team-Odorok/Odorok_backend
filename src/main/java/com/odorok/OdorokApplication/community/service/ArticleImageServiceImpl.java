package com.odorok.OdorokApplication.community.service;

import com.odorok.OdorokApplication.community.repository.ArticleImageRepository;
import com.odorok.OdorokApplication.draftDomain.ArticleImage;
import com.odorok.OdorokApplication.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleImageServiceImpl implements ArticleImageService{
    private final S3Service s3Service;

    private final ArticleImageRepository articleImageRepository;

    @Override
    public List<String> insertArticleImages(Long userId, List<MultipartFile> images) {
        return s3Service.uploadMany("community", String.valueOf(userId),images);
    }

    //articleId를 사용하여 db에서 url을 삭제하는 작업 - 삭제되어야 할 이미지url들을 반환한다.
    @Override
    public List<String> deleteArticleImageUrl(Long articleId) {
        List<String> urls = articleImageRepository.findImgUrlsByArticleId(articleId);
        articleImageRepository.deleteByArticleId(articleId);
        return urls;
    }

    //imageurl리스트를 db에 넣는 작업?
    @Override
    public void insertArticleImageUrl(Long userId, List<String> urls) {
        List<ArticleImage> images = new ArrayList();
        for(String url : urls){
            images.add(new ArticleImage(userId,url));
        }
        articleImageRepository.saveAll(images);
    }

    @Override
    public void deleteImages(List<String> urls) {
        s3Service.deleteMany(urls);
    }
}
