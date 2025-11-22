package com.example.LMS.service;

import com.example.LMS.Exception.AppException;
import com.example.LMS.Exception.ErrorCode;
import com.example.LMS.dto.ApiResponse;
import com.example.LMS.dto.Request.CourseQuery;
import com.example.LMS.dto.Request.CourseRequest;
import com.example.LMS.dto.Request.CourseUpdate;
import com.example.LMS.dto.Response.CourseResponse;
import com.example.LMS.dto.dtoProjection.CourseDTO;
import com.example.LMS.dto.dtoProjection.CourseImageDTO;
import com.example.LMS.entity.Course;
import com.example.LMS.entity.Image;
import com.example.LMS.enums.ImageType;
import com.example.LMS.enums.ObjectType;
import com.example.LMS.enums.Status;
import com.example.LMS.mapper.CourseMapper;
import com.example.LMS.repo.CourseRepo;
import com.example.LMS.repo.ImageRepo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jxls.common.Context;
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
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepo courseRepo;
    private final CourseMapper courseMapper;
    private final FileStorageService fileStorageService;
    private final ImageRepo imageRepo;
    private final MessageSource messageSource;

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

    public Page<CourseDTO> searchCourse(CourseQuery query){
        Pageable pageable = PageRequest.of(query.getPage(), query.getSize());

        Long count = courseRepo.count(query);
        if (count == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        List<CourseDTO> courses = courseRepo.search(query, pageable);
        List<Long> ids = courses.stream().map(CourseDTO::getId).toList();

        List<CourseImageDTO> coursesImg = courseRepo.findCoursesWithActiveThumbnails(ids);
        Map<Long, List<Image>> thumbnailsMap = coursesImg.stream()
                .collect(Collectors.groupingBy(
                        CourseImageDTO::getId,
                        Collectors.mapping(CourseImageDTO::getThumbnail, Collectors.toList())
                ));
        courses.forEach(course -> course.setThumbnail(
                thumbnailsMap.getOrDefault(course.getId(), List.of())));
        return new PageImpl<>(courses, pageable, count);
    }

    @Transactional(rollbackFor = Exception.class)
    public CourseResponse updateCourse(Long id, CourseUpdate courseUpdate, List<MultipartFile> images,
                                       List<Long> deleteThumbnailId, Long mainThumbnailId) throws IOException {
        //check Status
        Course course = courseRepo.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        courseMapper.updateCourse(courseUpdate, course);

        //Xoa mem cac thumbnail co trong list xoa
//        if(deleteThumbnailId != null && !deleteThumbnailId.isEmpty()){
//            for(Long deleteThumbnail : deleteThumbnailId){
//                Image image = imageRepo.findById(deleteThumbnail)
//                        .orElseThrow(() -> new AppException(ErrorCode.THUMBNAIL_NOT_FOUND));
//                image.setStatus(Status.DELETED);
//                imageRepo.save(image);
//            }
//        }
        if(deleteThumbnailId != null && !deleteThumbnailId.isEmpty()){

//            List<Image> imageList = imageRepo.findAllById(deleteThumbnailId);
            List<Image> imageList = imageRepo.findAllByIdInAndStatus(deleteThumbnailId,Status.ACTIVE);

            if (imageList.size() != deleteThumbnailId.size()) {
                throw new AppException(ErrorCode.THUMBNAIL_NOT_FOUND);
            }

            imageList.forEach(img -> img.setStatus(Status.DELETED));

            imageRepo.saveAll(imageList);
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
                        .anyMatch(img -> img.getId().equals(mainThumbnailId));

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
    public ApiResponse<Void> deleteCourse(Long id){
        Course course = courseRepo.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        course.setStatus(Status.DELETED);
        courseRepo.save(course);
        return ApiResponse.<Void>builder()
                .build();
    }


    public void exportCourse(HttpServletResponse response, CourseQuery query) {
        Long count = courseRepo.count(query);
        if (count == 0) {
            throw  new AppException(ErrorCode.NO_DATA_TO_EXPORT);
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
}