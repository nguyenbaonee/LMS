package com.example.LMS.controller;

import com.example.LMS.dto.ApiResponse;
import com.example.LMS.dto.Request.StudentQuery;
import com.example.LMS.dto.Request.StudentRequest;
import com.example.LMS.dto.Request.StudentUpdate;
import com.example.LMS.dto.Response.StudentResponse;
import com.example.LMS.enums.Status;
import com.example.LMS.service.StudentService;
import jakarta.servlet.http.HttpServletResponse;
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
    public Page<StudentResponse> searchStudent(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(required = false) String name,
                                               @RequestParam(required = false) String email,
                                               @RequestParam(required = false) Status status){
        return studentService.searchStudent(page,size,name,email,status);
    }

    @GetMapping("/{id}")
    public StudentResponse getStdById(@PathVariable Long id,@RequestParam(defaultValue = "ACTIVE") Status status){
        return studentService.getStudentDetail(id,status);
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
    @GetMapping("/export")
    public void exportStudent(HttpServletResponse response, StudentQuery query){
        studentService.export(response, query);
    }
}
