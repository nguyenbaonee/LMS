package com.example.LMS.dto.dtoProjection;

import com.example.LMS.entity.Course;
import com.example.LMS.entity.Image;
import com.example.LMS.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    Long id;
    String name;
    String code;
    List<Image> thumbnail;
    String description;
    Status status;

    public CourseDTO(Long id,String name, String code, String description, Status status) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.status = status;
    }
}
