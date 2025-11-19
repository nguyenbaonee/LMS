package com.example.LMS.service;

import com.example.LMS.Exception.AppException;
import com.example.LMS.Exception.ErrorCode;
import com.example.LMS.dto.Request.LessonRequest;
import com.example.LMS.dto.Response.LessonResponse;
import com.example.LMS.entity.Course;
import com.example.LMS.entity.Image;
import com.example.LMS.entity.Lesson;
import com.example.LMS.enums.ImageType;
import com.example.LMS.enums.ObjectType;
import com.example.LMS.enums.Status;
import com.example.LMS.mapper.LessonMapper;
import com.example.LMS.repo.CourseRepo;
import com.example.LMS.repo.ImageRepo;
import com.example.LMS.repo.LessonRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class LessonService {

    LessonRepo lessonRepo;
    LessonMapper lessonMapper;
    CourseRepo courseRepo;
    FileStorageService fileStorageService;
    ImageRepo imageRepo;

    public LessonService(LessonRepo lessonRepo, LessonMapper lessonMapper,
                         CourseRepo courseRepo, FileStorageService fileStorageService,
                         ImageRepo imageRepo) {
        this.lessonRepo = lessonRepo;
        this.lessonMapper = lessonMapper;
        this.courseRepo = courseRepo;
        this.fileStorageService = fileStorageService;
        this.imageRepo = imageRepo;
    }

    @Transactional(rollbackFor = Exception.class)
    public LessonResponse createLesson(Long courseId, LessonRequest lessonRequest, List<MultipartFile> images, List<MultipartFile> videos) throws IOException {

        Course course = courseRepo.findByIdAndStatus(courseId, Status.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        Lesson lesson = lessonMapper.toLesson(lessonRequest);
        lessonRepo.save(lesson);

        if(videos == null || videos.isEmpty()){
            throw new AppException(ErrorCode.VIDEO_NOT_EMPTY);
        }
        List<Image> videoList = new ArrayList<>();
        List<String> videoPaths = new ArrayList<>();

        List<Image> imgList = new ArrayList<>();
        List<String> imgPaths = new ArrayList<>();
        try {
            if (videos != null && !videos.isEmpty()) {

                List<Lesson> lessons = lessonRepo.findByCourseIdAndStatusOrderByLessonOrder(courseId, Status.ACTIVE);
                for(Lesson l : lessons){
                    if(l.getLessonOrder() >= lessonRequest.getLessonOrder()){
                        l.setLessonOrder(l.getLessonOrder() + 1);
                    }
                }
                lessonRepo.saveAll(lessons);

                for (MultipartFile file : videos) {
                    String url = fileStorageService.save(file, ObjectType.LESSON, ImageType.VIDEO);
                    Image video = new Image();
                    video.setUrl(url);
                    video.setObjectType(ObjectType.LESSON);
                    video.setType(ImageType.VIDEO);
                    video.setObjectId(lesson.getId());
                    videoList.add(video);
                    videoPaths.add(url);
                }
                imageRepo.saveAll(videoList);
                lesson.setVideoUrl(videoList);
                lessonRepo.save(lesson);
            }
            if (images == null || images.isEmpty()) {
            return lessonMapper.toLessonResponse(lesson);
        }
            boolean thumbnailMain = true;

            for (MultipartFile file : images) {
                String url = fileStorageService.save(file, ObjectType.LESSON, ImageType.THUMBNAIL);
                Image img = new Image();
                img.setUrl(url);
                img.setPrimary(thumbnailMain);
                thumbnailMain = false;
                img.setObjectType(ObjectType.LESSON);
                img.setType(ImageType.THUMBNAIL);
                img.setObjectId(lesson.getId());

                imgList.add(img);
                imgPaths.add(url);
            }
            imageRepo.saveAll(imgList);
            lesson.setThumbnail(imgList);
            lessonRepo.save(lesson);
            return lessonMapper.toLessonResponse(lesson);
        } catch (Exception e) {
            fileStorageService.deleteFiles(imgPaths);
            fileStorageService.deleteFiles(imgPaths);
            throw e;
        }
    }
    public LessonResponse updateLesson(Long lessonId, LessonRequest lessonRequest, List<MultipartFile> images, List<MultipartFile> videos,
                                       List<Long> deleteThumbnailId, Long mainThumbnailId,
                                       List<Long> deleteVideos) throws IOException {
        Lesson lesson = lessonRepo.findByIdAndStatus(lessonId, Status.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        lessonMapper.updateLesson(lessonRequest, lesson);
        if(deleteThumbnailId != null && !deleteThumbnailId.isEmpty()){
            for(Long deleteThumbnail : deleteThumbnailId){
                Image image = imageRepo.findById(deleteThumbnail)
                        .orElseThrow(() -> new AppException(ErrorCode.THUMBNAIL_NOT_FOUND));
                image.setStatus(Status.DELETED);
                imageRepo.save(image);
            }
        }

        return null;
    }
    public Page<LessonResponse> getLessonByCourse(int page, int size, Long courseId){
        if(!courseRepo.existsByIdAndStatus(courseId, Status.ACTIVE)){
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Lesson> lessons = lessonRepo.findByCourseId(courseId,pageable);
        return lessons.map(lesson -> lessonMapper.toLessonResponse(lesson));
    }
    public void deleteLesson(Long lessonId){
        Lesson lesson = lessonRepo.findByIdAndStatus(lessonId, Status.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        lesson.setStatus(Status.DELETED);
        lessonRepo.save(lesson);
    }
}
