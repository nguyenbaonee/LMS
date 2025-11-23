package com.example.LMS.service;

import com.example.LMS.Exception.AppException;
import com.example.LMS.Exception.ErrorCode;
import com.example.LMS.dto.ApiResponse;
import com.example.LMS.dto.Request.CourseQuery;
import com.example.LMS.dto.Request.CourseRequest;
import com.example.LMS.dto.Request.CourseUpdate;
import com.example.LMS.dto.Response.CourseResponse;
import com.example.LMS.dto.dtoProjection.*;
import com.example.LMS.entity.Course;
import com.example.LMS.entity.Image;
import com.example.LMS.enums.ImageType;
import com.example.LMS.enums.ObjectType;
import com.example.LMS.enums.Status;
import com.example.LMS.mapper.CourseMapper;
import com.example.LMS.repo.CourseRepo;
import com.example.LMS.repo.ImageRepo;
import jakarta.servlet.http.HttpServletResponse;
import org.jxls.util.JxlsHelper;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.jxls.common.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    MessageSource messageSource;
    LessonService lessonService;

    public CourseService(CourseRepo courseRepo, CourseMapper courseMapper,
                         FileStorageService fileStorageService, ImageRepo imageRepo,
                         MessageSource messageSource, LessonService lessonService) {
        this.courseRepo = courseRepo;
        this.courseMapper = courseMapper;
        this.fileStorageService = fileStorageService;
        this.imageRepo = imageRepo;
        this.messageSource = messageSource;
        this.lessonService = lessonService;
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
                    ImageDTO imageDTO = fileStorageService.save(file, ObjectType.COURSE, ImageType.THUMBNAIL);
                    Image img = new Image();
                    img.setUrl(imageDTO.getUrl());
                    img.setFileName(imageDTO.getFilename());
                    img.setPrimary(thumbnailMain);
                    thumbnailMain = false;
                    img.setObjectType(ObjectType.COURSE);
                    img.setType(ImageType.THUMBNAIL);
                    img.setObjectId(course.getId());

                    imgList.add(img);
                    paths.add(imageDTO.getUrl());
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

    public Page<CourseResponse> searchCourse(int page, int size, String name, String code, Status status) {
        if (name != null && !name.isBlank()) {
            name = "%" + name.replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_")
                    .toLowerCase() + "%";
        } else {
            name = null;
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Long> courseIds = courseRepo.searchIds(pageable, name, code, status);
        if (courseIds.getTotalElements() == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        List<Long> ids = courseIds.getContent();
        List<CourseImageDTO> coursesImg = courseRepo.findCoursesWithActiveThumbnails(ids);
        List<CourseDTO> courses = courseRepo.findCourseDTOsByIds1(ids, status);
        Map<Long, List<Image>> thumbnailsMap = coursesImg.stream()
                .collect(Collectors.groupingBy(
                        CourseImageDTO::getId,
                        Collectors.mapping(CourseImageDTO::getThumbnail, Collectors.toList())
                ));
        courses.forEach(course -> course.setThumbnail(
                thumbnailsMap.getOrDefault(course.getId(), List.of())));
        List<CourseResponse> courseResponseList = courseMapper.toResponseFromDTOs(courses);
        return new PageImpl<>(courseResponseList, pageable, courseIds.getTotalElements());
    }
    public CourseResponse getCourseDetail(Long id, Status status) {

        List<CourseImageDTO> imageDTOs = courseRepo.findCoursesWithActiveThumbnails(List.of(id));
        if(imageDTOs.isEmpty()){
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        // Lấy thông tin CourseDTO
        List<CourseDTO> courseDTOList = courseRepo.findCourseDTOsByIds1(List.of(id), status);

        // Gom images theo id
        Map<Long, List<Image>> imgMap = imageDTOs.stream()
                .collect(Collectors.groupingBy(
                        CourseImageDTO::getId,
                        Collectors.mapping(CourseImageDTO::getThumbnail, Collectors.toList())
                ));

        // Set img vào DTO
        CourseDTO dto = courseDTOList.get(0);
        dto.setThumbnail(imgMap.getOrDefault(id, List.of()));

        return courseMapper.toResponseFromDTO(dto);
    }
    @Transactional(rollbackFor = Exception.class)
    public CourseResponse updateCourse(Long id, CourseUpdate courseUpdate, List<MultipartFile> images,
                                       List<Long> deleteThumbnailId, Long mainThumbnailId) throws IOException {
        //check Status
        Course course = courseRepo.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        courseMapper.updateCourse(courseUpdate, course);

        if(deleteThumbnailId != null && !deleteThumbnailId.isEmpty()){

            List<Image> imageList = imageRepo.findAllByIdInAndObjectIdAndStatus(deleteThumbnailId, course.getId(), Status.ACTIVE);
            if (imageList.size() != deleteThumbnailId.size()) {
                throw new AppException(ErrorCode.THUMBNAIL_NOT_FOUND);
            }

            imageList.forEach(img -> img.setStatus(Status.DELETED));

            imageRepo.saveAll(imageList);
        }

        List<Image> thumbActive = imageRepo.findByObjectIdAndStatus(course.getId(), Status.ACTIVE);
        //xu ly thumb hien thi
        if(mainThumbnailId != null){
            boolean exists = thumbActive.stream()
                    .anyMatch(img -> img.getId().equals(mainThumbnailId));

            if (!exists) {
                throw new AppException(ErrorCode.THUMBNAIL_NOT_FOUND);
            }
            for(Image img : thumbActive) {
                img.setPrimary(img.getId().equals(mainThumbnailId));
            }
            imageRepo.saveAll(thumbActive);
        } else if(!thumbActive.isEmpty()){
            thumbActive.forEach(img -> img.setPrimary(false));
            thumbActive.get(0).setPrimary(true);
            imageRepo.saveAll(thumbActive);
        }

        //list anh moi
        List<Image> thumbnail = new ArrayList<>();
        List<String> savedFilePaths = new ArrayList<>();

        try{
            //luu anh moi
            if(images != null && !images.isEmpty()){
                for(MultipartFile file : images) {
                    ImageDTO imageDTO = fileStorageService.save(file, ObjectType.COURSE, ImageType.THUMBNAIL);
                    Image image = new Image();
                    image.setUrl(imageDTO.getUrl());
                    image.setFileName(imageDTO.getFilename());
                    image.setType(ImageType.THUMBNAIL);
                    image.setObjectType(ObjectType.COURSE);
                    image.setObjectId(course.getId());
                    thumbnail.add(image);
                    savedFilePaths.add(imageDTO.getUrl());
                }
            }
            //luu lai avatar
            course.getThumbnail().addAll(thumbnail);
            imageRepo.saveAll(thumbnail);
            courseRepo.save(course);

            return courseMapper.toCourseResponse(course);
        }
        catch (Exception e){
            fileStorageService.deleteFiles(savedFilePaths);
            throw e;
        }
    }
    public void exportCourse(HttpServletResponse response, CourseQuery query) {
        Long count = courseRepo.count(query);
        if (count == 0) {
            throw new AppException(ErrorCode.NO_DATA_TO_EXPORT);
        }
        List<CourseDTO> courses = courseRepo.search(query, null);

        try (
                InputStream templateStream = new ClassPathResource("templates/course_template.xlsx").getInputStream();
                OutputStream outputStream = response.getOutputStream()
        ) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String timestamp = LocalDateTime.now().format(formatter);
            String resultFileName = String.format("course_report_%s.xlsx", timestamp);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + resultFileName + "\"");

            Context context = new Context();
            context.putVar("lists", courses);

            JxlsHelper.getInstance().processTemplate(templateStream, outputStream, context);

            response.flushBuffer();
        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }
    }
    @Transactional
    public ApiResponse<Void> deleteCourse(Long id){
        Course course = courseRepo.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        course.setStatus(Status.DELETED);
        //xoa them lesson
        List<LessonDTO> lessonDTOS = lessonService.getLessonByCourse(0,10,id, Status.ACTIVE).getContent();
        for(LessonDTO lessonDTO : lessonDTOS){
            lessonService.deleteLesson(lessonDTO.getId());
        }
        courseRepo.save(course);
        return ApiResponse.<Void>builder()
                .build();
    }


}