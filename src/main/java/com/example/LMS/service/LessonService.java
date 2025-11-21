package com.example.LMS.service;

import com.example.LMS.Exception.AppException;
import com.example.LMS.Exception.ErrorCode;
import com.example.LMS.dto.ApiResponse;
import com.example.LMS.dto.Request.LessonRequest;
import com.example.LMS.dto.Response.LessonResponse;
import com.example.LMS.dto.dtoProjection.LessonDTO;
import com.example.LMS.dto.dtoProjection.LessonThumbDTO;
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
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LessonService {

    LessonRepo lessonRepo;
    LessonMapper lessonMapper;
    CourseRepo courseRepo;
    FileStorageService fileStorageService;
    ImageRepo imageRepo;
    MessageSource messageSource;

    public LessonService(LessonRepo lessonRepo, LessonMapper lessonMapper,
                         CourseRepo courseRepo, FileStorageService fileStorageService,
                         ImageRepo imageRepo, MessageSource messageSource) {
        this.lessonRepo = lessonRepo;
        this.lessonMapper = lessonMapper;
        this.courseRepo = courseRepo;
        this.fileStorageService = fileStorageService;
        this.imageRepo = imageRepo;
        this.messageSource = messageSource;
    }

    @Transactional(rollbackFor = Exception.class)
    public LessonResponse createLesson(
            Long courseId,
            LessonRequest lessonRequest,
            List<MultipartFile> images,
            List<MultipartFile> videos) throws IOException {

        Course course = courseRepo.findByIdAndStatus(courseId, Status.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        if (videos == null || videos.isEmpty()) {
            throw new AppException(ErrorCode.VIDEO_NOT_EMPTY);
        }

        List<String> videoPaths = new ArrayList<>();
        List<String> imgPaths = new ArrayList<>();

        try {

            List<Lesson> lessons = lessonRepo.findByCourseIdAndStatusOrderByLessonOrder(courseId, Status.ACTIVE);
            for (Lesson l : lessons) {
                if (l.getLessonOrder() >= lessonRequest.getLessonOrder()) {
                    l.setLessonOrder(l.getLessonOrder() + 1);
                }
            }
            lessonRepo.saveAll(lessons);
            lessonRepo.flush();

            Lesson lesson = lessonMapper.toLesson(lessonRequest);
            lesson.setCourse(course);
            lessonRepo.save(lesson);


            List<Image> videoList = new ArrayList<>();
            for (MultipartFile file : videos) {
                String url = fileStorageService.save(file, ObjectType.LESSON, ImageType.VIDEO);
                videoPaths.add(url);

                Image video = new Image();
                video.setUrl(url);
                video.setObjectType(ObjectType.LESSON);
                video.setType(ImageType.VIDEO);
                video.setObjectId(lesson.getId());
                videoList.add(video);
            }
            imageRepo.saveAll(videoList);
            lesson.setVideoUrl(videoList);


            if (images != null && !images.isEmpty()) {
                List<Image> imgList = new ArrayList<>();
                boolean primary = true;

                for (MultipartFile file : images) {
                    String url = fileStorageService.save(file, ObjectType.LESSON, ImageType.THUMBNAIL);
                    imgPaths.add(url);

                    Image img = new Image();
                    img.setUrl(url);
                    img.setPrimary(primary);
                    primary = false;
                    img.setObjectType(ObjectType.LESSON);
                    img.setType(ImageType.THUMBNAIL);
                    img.setObjectId(lesson.getId());

                    imgList.add(img);
                }

                imageRepo.saveAll(imgList);
                lesson.setThumbnail(imgList);
            }

            lessonRepo.save(lesson);
            return lessonMapper.toLessonResponse(lesson);

        } catch (Exception ex) {

            fileStorageService.deleteFiles(videoPaths);
            fileStorageService.deleteFiles(imgPaths);

            throw ex;
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
    public Page<LessonDTO> getLessonByCourse(int page, int size, Long courseId){
        if(!courseRepo.existsByIdAndStatus(courseId, Status.ACTIVE)){
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<LessonDTO> lessons = lessonRepo.findByCourseId(courseId,pageable);
        List<Long> lessonIds = lessons.getContent().stream().map(LessonDTO::getId).toList();
        List<LessonThumbDTO> lessonThumbDTOS = lessonRepo.findLessonThumb(lessonIds);

        Map<Long,List<Image>> thumbMap = lessonThumbDTOS.stream()
                .collect(Collectors.groupingBy(
                        LessonThumbDTO::getId,
                        Collectors.mapping(LessonThumbDTO::getThumbnail, Collectors.toList())
                ));
        List<LessonDTO> lessonDTOS = lessons.getContent();
        lessonDTOS.forEach(lessonDTO -> lessonDTO.setThumbnails(
                thumbMap.getOrDefault(lessonDTO.getId(), List.of())));
        return new PageImpl<>(lessonMapper.toLessonResponseFroms(lessonDTOS),pageable,lessons.getTotalElements());
    }
    public ApiResponse<Void> deleteLesson(Long lessonId){
        Lesson lesson = lessonRepo.findByIdAndStatus(lessonId, Status.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        lesson.setStatus(Status.DELETED);
        lessonRepo.save(lesson);
        return ApiResponse.<Void>builder()
                .build();
    }
}
