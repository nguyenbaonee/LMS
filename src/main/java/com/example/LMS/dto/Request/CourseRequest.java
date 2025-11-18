package com.example.LMS.dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequest {
    @NotBlank(message = "valid.notBlank")
    @Length(min = 1, max = 150, message = "valid.course.length")
    String name;

    @NotBlank @Length(min = 1, max = 50)
    String code;

    String description;
}
