package com.example.LMS.dto.dtoProjection;

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
    private Long id;
    private String name;
    private String code;
    private List<Image> thumbnail;
    private String description;
    private Status status;

    public CourseDTO(Long id,String name, String code, String description, Status status) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.status = status;
    }
}
