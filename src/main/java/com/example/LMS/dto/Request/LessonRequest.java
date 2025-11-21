package com.example.LMS.dto.Request;

import com.example.LMS.entity.Course;
import com.example.LMS.entity.Image;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonRequest {

    @NotBlank(message = "{valid.notBlank}")
    @Length(min = 1, max = 200, message = "{valid.lesson.length}")
    String title;

    @Min(message = "{valid.lesson.length}", value = 1)
    @Max(value = 200, message ="{valid.lesson.length}" )
    @NotNull(message = "{valid.notBlank}")
    Integer lessonOrder;
}
