package com.example.LMS.dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseUpdate {
    @NotBlank(message = "{valid.notBlank}")
    @Length(min = 1, max = 150, message = "{valid.course.length}")
    String name;

    String description;
}
