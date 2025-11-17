package com.example.LMS.dto.Response;

import com.example.LMS.entity.Enrollment;
import com.example.LMS.entity.Image;
import com.example.LMS.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {
    Long id;

    String name;

    String email;

    List<Image> avatar;

    Status status = Status.ACTIVE;

    List<EnrollmentResponse> enrollments;
}
