package com.example.LMS.dto.Response;

import com.example.LMS.entity.Image;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonResponse {

    String title;

    List<Image> videoUrl;

    List<Image> thumbnail;

    Integer lessonOrder;
}
