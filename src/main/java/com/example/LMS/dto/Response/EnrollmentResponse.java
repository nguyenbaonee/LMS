package com.example.LMS.dto.Response;

import com.example.LMS.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {
    Long id;

    Course course;

    LocalDateTime enrolledAt;
}
