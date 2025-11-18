package com.example.LMS.service;

import com.example.LMS.Exception.AppException;
import com.example.LMS.Exception.ErrorCode;
import com.example.LMS.dto.Request.CourseRequest;
import com.example.LMS.dto.Request.LessonRequest;
import com.example.LMS.dto.Response.CourseResponse;
import com.example.LMS.dto.Response.LessonResponse;
import com.example.LMS.entity.Course;
import com.example.LMS.entity.Image;
import com.example.LMS.enums.ImageType;
import com.example.LMS.enums.ObjectType;
import com.example.LMS.mapper.LessonMapper;
import com.example.LMS.repo.LessonRepo;
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

    public LessonService(LessonRepo lessonRepo, LessonMapper lessonMapper) {
        this.lessonRepo = lessonRepo;
        this.lessonMapper = lessonMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public LessonResponse createLesson(LessonRequest lessonRequest, List<MultipartFile> images, List<MultipartFile> video) throws IOException {
        if (courseRepo.existsByCode(courseRequest.getCode())) {
            throw new AppException(ErrorCode.CODECOURSEEXISTS);
        }
        Course course = courseMapper.toCourse(courseRequest);
        courseRepo.save(course);

        if (images == null || images.isEmpty()) {
            return courseMapper.toCourseResponse(course);
        }
        List<Image> imgList = new ArrayList<>();
        List<String> paths = new ArrayList<>();
        try {
            if (images != null && !images.isEmpty()) {

                boolean thumbnailMain = true;

                for (MultipartFile file : images) {
                    String url = fileStorageService.save(file, ObjectType.COURSE, ImageType.THUMBNAIL);
                    Image img = new Image();
                    img.setUrl(url);
                    img.setPrimary(thumbnailMain);
                    thumbnailMain = false;
                    img.setObjectType(ObjectType.COURSE);
                    img.setType(ImageType.THUMBNAIL);
                    img.setObjectId(course.getId());

                    imgList.add(img);
                    paths.add(url);
                }
                imageRepo.saveAll(imgList);
                course.setThumbnail(imgList);
                courseRepo.save(course);
                return courseMapper.toCourseResponse(course);
            }
        } catch (Exception e) {
            fileStorageService.deleteFiles(paths);
            throw e;
        }
        return courseMapper.toCourseResponse(course);
    }

}
