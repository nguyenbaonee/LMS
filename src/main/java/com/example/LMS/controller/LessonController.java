package com.example.LMS.controller;

import com.example.LMS.dto.ApiResponse;
import com.example.LMS.dto.Request.LessonRequest;
import com.example.LMS.dto.Response.LessonResponse;
import com.example.LMS.dto.dtoProjection.LessonDTO;
import com.example.LMS.service.LessonService;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

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
        Locale locale = LocaleContextHolder.getLocale();
        return ApiResponse.<LessonResponse>builder()
                .code(HttpStatus.OK.value())
                .message(messageSource.getMessage("response.success", null, locale))
                .data(lessonService.createLesson(courseId, lessonRequest, images, videos))
                .build();
    }
    @GetMapping
    public Page<LessonDTO> getLessonByCourse(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam("courseId") Long courseId){
        return lessonService.getLessonByCourse(page, size, courseId);
    }
    @DeleteMapping("/{lessonId}")
    public void deleteLesson(@PathVariable Long lessonId){
        lessonService.deleteLesson(lessonId);
    }
}
