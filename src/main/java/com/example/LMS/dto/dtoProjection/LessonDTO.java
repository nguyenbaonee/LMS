package com.example.LMS.dto.dtoProjection;

import com.example.LMS.entity.Image;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class LessonDTO {
    Long id;
    String title;
    Integer lessonOrder;
    List<Image> thumbnails;

    public LessonDTO(Long id, String title, Integer lessonOrder) {
        this.id = id;
        this.title = title;
        this.lessonOrder = lessonOrder;
    }

}
