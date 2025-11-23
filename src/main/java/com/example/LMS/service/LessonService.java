package com.example.LMS.service;

import com.example.LMS.Exception.AppException;
import com.example.LMS.Exception.ErrorCode;
import com.example.LMS.dto.ApiResponse;
import com.example.LMS.dto.Request.LessonRequest;
import com.example.LMS.dto.Response.LessonResponse;
import com.example.LMS.dto.dtoProjection.ImageDTO;
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
                ImageDTO imageDTO = fileStorageService.save(file, ObjectType.LESSON, ImageType.VIDEO);
                videoPaths.add(imageDTO.getUrl());

                Image video = new Image();
                video.setUrl(imageDTO.getUrl());
                video.setFileName(imageDTO.getFilename());
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
                    ImageDTO imageDTO = fileStorageService.save(file, ObjectType.LESSON, ImageType.THUMBNAIL);
                    imgPaths.add(imageDTO.getUrl());

                    Image img = new Image();
                    img.setUrl(imageDTO.getUrl());
                    img.setFileName(imageDTO.getFilename());
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
    @Transactional
    public LessonResponse updateLesson(Long lessonId, LessonRequest lessonRequest, List<MultipartFile> images,
                                       List<MultipartFile> videos,
                                       List<Long> deleteThumbnailId, Long mainThumbnailId,
                                       List<Long> deleteVideos) throws IOException {
        Lesson lesson = lessonRepo.findByIdAndStatus(lessonId, Status.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        lessonMapper.updateLesson(lessonRequest, lesson);
        if(deleteThumbnailId != null && !deleteThumbnailId.isEmpty()){
            List<Image> imageList = imageRepo.findAllByIdInAndObjectIdAndStatus(deleteThumbnailId,
                    lesson.getId(), Status.ACTIVE);
            if(imageList.size() != deleteThumbnailId.size()){
                throw new AppException(ErrorCode.THUMBNAIL_NOT_FOUND);
            }
            imageList.forEach(img -> img.setStatus(Status.DELETED));
            imageRepo.saveAll(imageList);
        }

        List<Image> thumbActive = imageRepo.findByObjectIdAndStatus(lesson.getId(), Status.ACTIVE);
        if(mainThumbnailId != null){
            boolean exists = thumbActive.stream().anyMatch(img -> img.getId().equals(mainThumbnailId));
            if(!exists){
                throw new AppException(ErrorCode.THUMBNAIL_NOT_FOUND);
            }
            thumbActive.forEach(img -> img.setPrimary(img.getId().equals(mainThumbnailId)));
            imageRepo.saveAll(thumbActive);
        } else if(!thumbActive.isEmpty()){
            thumbActive.forEach(img -> img.setPrimary(false));
            thumbActive.get(0).setPrimary(true);
            imageRepo.saveAll(thumbActive);
        }

        if(deleteVideos != null && !deleteVideos.isEmpty()){
            List<Image> videoList = imageRepo.findAllByIdInAndObjectIdAndStatus(deleteVideos,
                    lesson.getId(), Status.ACTIVE);
            if(videoList.size() != deleteVideos.size()){
                throw new AppException(ErrorCode.VIDEO_NOT_EMPTY);
            }
            videoList.forEach(v -> v.setStatus(Status.DELETED));
            imageRepo.saveAll(videoList);
        }

        List<Image> newThumbnails = new ArrayList<>();
        List<Image> newVideos = new ArrayList<>();
        List<String> savedFilePaths = new ArrayList<>();

        try {
            // Lưu ảnh
            if(images != null && !images.isEmpty()){
                for(MultipartFile file : images){
                    ImageDTO imageDTO = fileStorageService.save(file, ObjectType.LESSON, ImageType.THUMBNAIL);
                    Image img = new Image();
                    img.setUrl(imageDTO.getUrl());
                    img.setFileName(imageDTO.getFilename());
                    img.setType(ImageType.THUMBNAIL);
                    img.setObjectType(ObjectType.LESSON);
                    img.setObjectId(lesson.getId());
                    newThumbnails.add(img);
                    savedFilePaths.add(imageDTO.getUrl());
                }
                imageRepo.saveAll(newThumbnails);
                lesson.getThumbnail().addAll(newThumbnails);
            }

            // Lưu video
            if(videos != null && !videos.isEmpty()){
                for(MultipartFile file : videos){
                    ImageDTO videoDTO = fileStorageService.save(file, ObjectType.LESSON, ImageType.VIDEO);
                    Image video = new Image();
                    video.setUrl(videoDTO.getUrl());
                    video.setFileName(videoDTO.getFilename());
                    video.setType(ImageType.THUMBNAIL);
                    video.setObjectType(ObjectType.LESSON);
                    video.setObjectId(lesson.getId());
                    newThumbnails.add(video);
                    savedFilePaths.add(videoDTO.getUrl());
                    video.setUrl(videoDTO.getUrl());
                    video.setFileName(videoDTO.getFilename());
                    video.setObjectType(ObjectType.LESSON);
                    video.setObjectId(lesson.getId());
                    newVideos.add(video);
                    savedFilePaths.add(videoDTO.getUrl());
                }
                imageRepo.saveAll(newVideos);
                lesson.getVideoUrl().addAll(newVideos);
            }

            lessonRepo.save(lesson);

            return lessonMapper.toLessonResponse(lesson);
        } catch(Exception e){
            // rollback file nếu có lỗi
            fileStorageService.deleteFiles(savedFilePaths);
            throw e;
        }
    }
    public Page<LessonDTO> getLessonByCourse(int page, int size, Long courseId, Status status){
        if(!courseRepo.existsByIdAndStatus(courseId, Status.ACTIVE)){
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<LessonDTO> lessons = lessonRepo.findByCourseId(courseId,pageable, status);
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
    @Transactional
    public ApiResponse<Void> deleteLesson(Long lessonId){
        Lesson lesson = lessonRepo.findByIdAndStatus(lessonId, Status.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        lesson.setStatus(Status.DELETED);
        lessonRepo.save(lesson);
        return ApiResponse.<Void>builder()
                .build();
    }
}
