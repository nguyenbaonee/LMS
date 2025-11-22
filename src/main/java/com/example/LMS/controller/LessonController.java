package com.example.LMS.controller;

import com.example.LMS.dto.ApiResponse;
import com.example.LMS.dto.Request.LessonQuery;
import com.example.LMS.dto.Request.LessonRequest;
import com.example.LMS.dto.Response.LessonResponse;
import com.example.LMS.dto.dtoProjection.LessonDTO;
import com.example.LMS.service.LessonService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {
    LessonService lessonService;
    MessageSource messageSource;

    public LessonController(LessonService lessonService,MessageSource messageSource) {
        this.lessonService = lessonService;
        this.messageSource = messageSource;
    }
    @PostMapping("/{courseId}")
    public ApiResponse<LessonResponse> createLesson(@PathVariable Long courseId, @Valid @ModelAttribute LessonRequest lessonRequest,
                                    @RequestParam(value = "images", required = false) List<MultipartFile> images,
                                    @RequestParam(value = "videos") List<MultipartFile> videos) throws IOException {
        return ApiResponse.of(lessonService.createLesson(courseId, lessonRequest, images, videos));
    }
    @GetMapping
    public ApiResponse<Page<LessonDTO>> search(LessonQuery query){
        return ApiResponse.of(lessonService.search(query));
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response, LessonQuery query){
        lessonService.export(response, query);
    }

    @DeleteMapping("/{lessonId}")
    public ApiResponse<Void> deleteLesson(@PathVariable Long lessonId){
        lessonService.deleteLesson(lessonId);
        return ApiResponse.ok();
    }
}
