package com.example.LMS.controller;

import com.example.LMS.dto.Request.EnrollmentRequest;
import com.example.LMS.dto.Request.EnrollmentUpdate;
import com.example.LMS.dto.Response.EnrollmentResponse;
import com.example.LMS.dto.Response.StudentResponse;
import com.example.LMS.enums.Status;
import com.example.LMS.service.EnrollmentService;
import org.springframework.data.domain.Page;
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
    public List<EnrollmentResponse> createEnrollment(@RequestBody EnrollmentRequest enrollmentRequest){
        return enrollmentService.createEnrollment(enrollmentRequest);
    }
    @PutMapping("/{enrollmentId}")
    public void updateEnrollment(@PathVariable Long enrollmentId,@RequestBody EnrollmentUpdate enrollmentUpdate) {
        enrollmentService.updateEnrollment(enrollmentId, enrollmentUpdate);
    }
    @GetMapping("/{courseId}")
    public Page<StudentResponse> getStudentsOfCourse(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "5") int size,
                                                     @PathVariable Long courseId,
                                                     @RequestParam(defaultValue = "ACTIVE") Status status) {
        return enrollmentService.getStudentsOfCourse(page, size, courseId,status);
    }
    @GetMapping("/student/{studentId}")
    public Page<EnrollmentResponse> getEnrollmentsOfStudent(@RequestParam (defaultValue = "0") int page,
                                                            @RequestParam (defaultValue = "5") int size,
                                                            @PathVariable Long studentId,
                                                            @RequestParam (defaultValue = "ACTIVE") Status status) {
        return enrollmentService.getEnrollmentsByStudent(page, size, studentId,status);
    }
    @DeleteMapping("/{id}")
    public void deleteEnrollment(@PathVariable Long id) {
        enrollmentService.deleteEnrollment(id);
    }
}
