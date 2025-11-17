package com.example.LMS.mapper;

import com.example.LMS.dto.Request.EnrollmentRequest;
import com.example.LMS.dto.Response.EnrollmentResponse;
import com.example.LMS.entity.Enrollment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EnrollmentMapper {
    Enrollment toEnrollment(EnrollmentRequest enrollmentRequest);
    EnrollmentResponse toEnrollmentResponse(Enrollment enrollment);

    List<Enrollment> toEnrollments(List<EnrollmentRequest> enrollmentRequests);
    List<EnrollmentResponse> toEnrollmentResponses(List<Enrollment> enrollments);
}
