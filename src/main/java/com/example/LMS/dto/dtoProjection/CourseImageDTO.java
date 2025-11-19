package com.example.LMS.dto.dtoProjection;

import com.example.LMS.entity.Image;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseImageDTO {
    Long id;
    Image thumbnail;
}
