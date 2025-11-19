package com.example.LMS.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {

    Long id;

    String name;

    String code;

    List<ImageResponse> thumbnail;

    String description;
}
