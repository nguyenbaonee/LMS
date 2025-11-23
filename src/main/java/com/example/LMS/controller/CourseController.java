package com.example.LMS.controller;

import com.example.LMS.dto.Request.CourseRequest;
import com.example.LMS.dto.Request.CourseUpdate;
import com.example.LMS.dto.Response.CourseResponse;
import com.example.LMS.entity.Course;
import com.example.LMS.enums.Status;
import com.example.LMS.repo.CourseRepo;
import com.example.LMS.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    CourseService courseService;
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CourseResponse createCourse(@Valid @ModelAttribute CourseRequest courseRequest,
                                       @RequestParam(value = "images", required = false) List<MultipartFile> images) throws IOException {
        return courseService.createCourse(courseRequest,images);
    }

    @GetMapping
    public Page<CourseResponse> searchCourse(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(required = false) String name,
                                             @RequestParam(required = false) String code,
                                             @RequestParam(defaultValue = "ACTIVE") Status status) {
        return courseService.searchCourse(page, size, name, code,status);
    }

    @PutMapping("/{id}")
    public CourseResponse updateCourse(@PathVariable Long id,@Valid @ModelAttribute CourseUpdate courseUpdate,
                                       @RequestParam(value = "images", required = false) List<MultipartFile> images,
                                       @RequestParam(value = "deleteThumbnailId", required = false) List<Long> deleteThumbnailId,
                                       @RequestParam(value = "mainThumbnailId", required = false) Long mainThumbnailId) throws IOException{
        return courseService.updateCourse(id,courseUpdate,images,deleteThumbnailId,mainThumbnailId);
    }
    @GetMapping("/{id}")
    public CourseResponse getCourseById(@PathVariable Long id, @RequestParam(defaultValue = "ACTIVE") Status status) {
        return courseService.getCourseDetail(id,status);
    }
    @DeleteMapping("/{id}")
    public void deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
    }

}
