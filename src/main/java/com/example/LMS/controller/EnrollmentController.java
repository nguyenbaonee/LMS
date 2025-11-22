package com.example.LMS.controller;

import com.example.LMS.dto.ApiResponse;
import com.example.LMS.dto.Request.EnrollmentRequest;
import com.example.LMS.dto.Request.EnrollmentUpdate;
import com.example.LMS.dto.Response.EnrollmentResponse;
import com.example.LMS.service.EnrollmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {
    EnrollmentService enrollmentService;
    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    public ApiResponse<List<EnrollmentResponse>> createEnrollment(@RequestBody EnrollmentRequest enrollmentRequest){
        return ApiResponse.of(enrollmentService.createEnrollment(enrollmentRequest));
    }
    @PutMapping("/{studentId}")
    public ApiResponse<Void> updateEnrollment(@PathVariable Long studentId,@RequestBody EnrollmentUpdate enrollmentUpdate) {
        enrollmentService.updateEnrollment(studentId, enrollmentUpdate);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteEnrollment(@PathVariable Long id) {
        enrollmentService.deleteEnrollment(id);
        return ApiResponse.ok();
    }
}
