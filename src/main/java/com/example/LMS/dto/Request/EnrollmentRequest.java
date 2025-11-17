package com.example.LMS.dto.Request;

import com.example.LMS.entity.Course;
import com.example.LMS.entity.Student;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRequest {
    Long id;

    Student student;

    Course course;
}
