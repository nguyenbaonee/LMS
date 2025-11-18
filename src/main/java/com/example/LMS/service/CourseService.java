package com.example.LMS.service;

import com.example.LMS.Exception.AppException;
import com.example.LMS.Exception.ErrorCode;
import com.example.LMS.dto.Request.CourseRequest;
import com.example.LMS.dto.Request.CourseUpdate;
import com.example.LMS.dto.Response.CourseResponse;
import com.example.LMS.entity.Course;
import com.example.LMS.entity.Image;
import com.example.LMS.enums.ImageType;
import com.example.LMS.enums.ObjectType;
import com.example.LMS.enums.Status;
import com.example.LMS.mapper.CourseMapper;
import com.example.LMS.repo.CourseRepo;
import com.example.LMS.repo.ImageRepo;
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
public class CourseService {
    CourseRepo courseRepo;
    CourseMapper courseMapper;
    FileStorageService fileStorageService;
    ImageRepo imageRepo;

    public CourseService(CourseRepo courseRepo, CourseMapper courseMapper,
                         FileStorageService fileStorageService, ImageRepo imageRepo) {
        this.courseRepo = courseRepo;
        this.courseMapper = courseMapper;
        this.fileStorageService = fileStorageService;
        this.imageRepo = imageRepo;
    }


    @Transactional(rollbackFor = Exception.class)
    public CourseResponse createCourse(CourseRequest courseRequest, List<MultipartFile> images) throws IOException {
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

    public Page<CourseResponse> searchCourse(int page, int size, String name, String code){
        if (name != null && !name.isBlank()) {
            name = "%" + name.replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_")
                    .toLowerCase() + "%";
        } else {
            name = null;
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Long> courseIds = courseRepo.searchIds(pageable, name, code);
        if (courseIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        List<Long> ids = courseIds.getContent();
        List<Course> courses = courseRepo.findAllByImage(ids);

        Map<Long, Course> courseMapId = courses.stream()
                .collect(Collectors.toMap(Course::getId, c -> c));

        List<Course> courseResponse = ids.stream()
                .map(courseMapId::get)
                .toList();
        List<CourseResponse> courseResponseList = courseMapper.toCourseResponses(courseResponse);
        return new PageImpl<>(courseResponseList, pageable, courseIds.getTotalElements());
    }

    @Transactional(rollbackFor = Exception.class)
    public CourseResponse updateCourse(Long id, CourseUpdate courseUpdate, List<MultipartFile> images,
                                       List<Long> deleteThumbnailId, Long mainThumbnailId) throws IOException {
        //check theo Id va Status
        Course course = courseRepo.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        courseMapper.updateCourse(courseUpdate, course);

        //Xoa mem cac thumbnail co trong list xoa
        if(deleteThumbnailId != null && !deleteThumbnailId.isEmpty()){
            for(Long deleteThumbnail : deleteThumbnailId){
                Image image = imageRepo.findById(deleteThumbnail)
                        .orElseThrow(() -> new AppException(ErrorCode.THUMBNAIL_NOT_FOUND));
                image.setStatus(Status.DELETED);
                imageRepo.save(image);
            }
        }

        //list anh moi
        List<Image> thumbnail = new ArrayList<>();
        List<String> savedFilePaths = new ArrayList<>();

        try{
            //luu anh moi
            if(images != null && !images.isEmpty()){
                for(MultipartFile file : images) {
                    String path = fileStorageService.save(file, ObjectType.COURSE, ImageType.THUMBNAIL);
                    Image image = new Image();
                    image.setUrl(path);
                    image.setType(ImageType.THUMBNAIL);
                    image.setObjectType(ObjectType.COURSE);
                    image.setObjectId(course.getId());
                    thumbnail.add(image);
                    savedFilePaths.add(path);
                }
            }
            //luu lai avatar
            course.getThumbnail().addAll(thumbnail);
            imageRepo.saveAll(thumbnail);
            courseRepo.save(course);

            List<Image> thumbActive = course.getThumbnail().stream()
                    .filter(img -> img.getStatus() == Status.ACTIVE)
                    .toList();

            //xu ly thumb hien thi
            if(mainThumbnailId != null){
                boolean exists = thumbActive.stream()
                        .anyMatch(img -> img.getId().equals(thumbActive));

                if (!exists) {
                    throw new AppException(ErrorCode.THUMBNAIL_NOT_FOUND);
                }
                for(Image img : thumbActive) {
                    img.setPrimary(img.getId().equals(mainThumbnailId));
                }
            } else if(!thumbActive.isEmpty()){
                thumbActive.forEach(img -> img.setPrimary(false));
                thumbActive.get(0).setPrimary(true);
            }
            return courseMapper.toCourseResponse(course);
        }
        catch (Exception e){
            fileStorageService.deleteFiles(savedFilePaths);
            throw e;
        }
    }

    @Transactional
    public void deleteCourse(Long id){
        Course course = courseRepo.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        course.setStatus(Status.DELETED);
        courseRepo.save(course);
    }


}