package com.example.LMS.controller;

import com.example.LMS.dto.ApiResponse;
import com.example.LMS.dto.Request.StudentQuery;
import com.example.LMS.dto.Request.StudentRequest;
import com.example.LMS.dto.Request.StudentUpdate;
import com.example.LMS.dto.Response.StudentResponse;
import com.example.LMS.dto.dtoProjection.StudentDTO;
import com.example.LMS.enums.Status;
import com.example.LMS.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/api/students")
public class StudentController {
    StudentService studentService;
    MessageSource messageSource;
    public StudentController(StudentService studentService, MessageSource messageSource) {
        this.studentService = studentService;
        this.messageSource = messageSource;
    }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StudentResponse createStd(@Valid @ModelAttribute StudentRequest studentRequest,
                                     @RequestParam("images") List<MultipartFile> images) throws IOException {
        return studentService.createStd(studentRequest,images);
    }

    @GetMapping
    public Page<StudentDTO> searchStudent(StudentQuery query){
        return studentService.searchStudent(query);
    }

    @PutMapping("/{id}")
    public StudentResponse updateStd(@PathVariable Long id,@Valid @ModelAttribute StudentUpdate studentUpdate,
                                     @RequestParam(value = "images", required = false) List<MultipartFile> images,
                                     @RequestParam(value = "deleteAvatarsId", required = false) List<Long> deleteAvatarsId,
                                     @RequestParam(value = "mainAvatarId", required = false) Long mainAvatarId) throws IOException {
        return studentService.updateStudent(id, studentUpdate, images, deleteAvatarsId, mainAvatarId);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteStd(@PathVariable Long id){
        studentService.deleteStudent(id);
        return ApiResponse.<Void>builder()
                .build();
    }
}
