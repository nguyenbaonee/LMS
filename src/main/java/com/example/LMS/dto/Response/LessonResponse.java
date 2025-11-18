package com.example.LMS.dto.Response;

import com.example.LMS.entity.Image;

import java.util.List;

public class LessonResponse {

    String title;

    List<Image> videoUrl;

    List<Image> thumbnail;

    Integer lessonOrder;
}
