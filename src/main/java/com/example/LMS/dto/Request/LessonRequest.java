package com.example.LMS.dto.Request;

import com.example.LMS.entity.Course;
import com.example.LMS.entity.Image;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonRequest {

    @NotBlank(message = "valid.notBlank")
    @Length(min = 1, max = 200, message = "valid.lesson.length")
    String title;

    List<Image> videoUrl;

    List<Image> thumbnail;

    @NotBlank(message = "valid.notBlank")
    Integer lessonOrder;
}
