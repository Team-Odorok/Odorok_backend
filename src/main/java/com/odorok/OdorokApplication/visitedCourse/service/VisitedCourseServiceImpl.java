package com.odorok.OdorokApplication.visitedCourse.service;

import com.odorok.OdorokApplication.commons.exception.NotFoundException;
import com.odorok.OdorokApplication.diary.dto.gpt.VisitedAdditionalAttraction;
import com.odorok.OdorokApplication.diary.repository.VisitedCourseRepository;
import com.odorok.OdorokApplication.domain.VisitedCourse;
import com.odorok.OdorokApplication.s3.service.S3Service;
import com.odorok.OdorokApplication.visitedCourse.dto.dto.CourseReview;
import com.odorok.OdorokApplication.visitedCourse.dto.dto.VisitedCourseView;
import com.odorok.OdorokApplication.visitedCourse.dto.dto.VisitedCourseInfo;
import com.odorok.OdorokApplication.visitedCourse.dto.dto.VisitedCourseView;
import com.odorok.OdorokApplication.visitedCourse.dto.response.VisitedCourseDetail;
import com.odorok.OdorokApplication.visitedCourse.dto.response.VisitedCourseResponseInfo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitedCourseServiceImpl implements VisitedCourseService {

    private final VisitedCourseRepository visitedCourseRepository;
    private final S3Service s3Service;

    @Override
    public VisitedCourseResponseInfo getVisitedCourses(Long userId) {
        List<VisitedCourseView> courseList = visitedCourseRepository.findVisitedCoursesAndReviewByUserId(userId);
        List<CourseReview> reviewList = new ArrayList<>();
        List<VisitedCourseInfo> courseInfoList = new ArrayList<>();
        for(VisitedCourseView vc : courseList){
            courseInfoList.add(VisitedCourseInfo.builder().courseName(vc.getCourseName()).visitedAt(vc.getVisitedAt())
                    .distance(vc.getDistance()).courseId(vc.getCourseId()).build());
            if(vc.getReview()!=null || vc.getStars()!=null){
                reviewList.add(CourseReview.builder().review(vc.getReview()).stars(vc.getStars()).courseName(vc.getCourseName())
                        .courseId(vc.getCourseId()).build());
            }
        }
        return VisitedCourseResponseInfo.builder().coursesList(courseInfoList).reviewList(reviewList).build();
    }

    @Override
    public VisitedCourseDetail getVisitedCourseDetail(Long userId, Long visitedCourseId) {
        // 방문코스 유효성 확인
        VisitedCourseDetail detail = visitedCourseRepository.findDetailById(userId, visitedCourseId)
                .orElseThrow(() -> new NotFoundException("해당 방문 코스 정보를 찾을 수 없거나 소유자가 아닙니다."));

        // 방문 명소 리스트
        List<VisitedAdditionalAttraction> attractions = visitedCourseRepository.findVisitedAttractionByVisitedCourseId(userId, visitedCourseId);
        detail.setVisitedAttractions(attractions);

        // 코스 평균 별점
        Double avgStars = visitedCourseRepository.findAvgStarsByCourseId(detail.getCourseId());
        detail.setAverageStars(avgStars);

        return detail;
    }

    @Override
    @Transactional
    public void createOrUpdateReview(Long userId, Long visitedCourseId, int star, String review, MultipartFile reviewImage) {
        // visitedCourse 조회
        VisitedCourse visitedCourse = visitedCourseRepository.findByCourseIdAndUserId(visitedCourseId,userId)
                .orElseThrow(() -> new NotFoundException("해당 방문 코스 정보를 찾을 수 없습니다."));

        // 후기 이미지 업로드
        if (reviewImage != null && !reviewImage.isEmpty()) {
            // 새 이미지 업로드
            List<String> imageUrls = s3Service.uploadMany("reviews", userId.toString(), List.of(reviewImage));
            visitedCourse.setReviewImgUrl(imageUrls.get(0));
        }

        visitedCourse.setStars(star);
        visitedCourse.setReview(review);
        visitedCourseRepository.save(visitedCourse);
    }
}
