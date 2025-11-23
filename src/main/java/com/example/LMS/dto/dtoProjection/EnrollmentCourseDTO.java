package com.example.LMS.dto.dtoProjection;

import com.example.LMS.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentCourseDTO {
    Long enrollmentId;
    LocalDateTime enrolledAt;
    CourseDTO course;

    public EnrollmentCourseDTO(Long enrollmentId, LocalDateTime enrolledAt,
                               Long courseId, String name, String code,
                               String description, Status status) {
        this.enrollmentId = enrollmentId;
        this.enrolledAt = enrolledAt;
        this.course = new CourseDTO(courseId, name, code, description, status);
    }
}

