package com.example.LMS.dto.Request;

import com.example.LMS.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonQuery extends SearchQuery{
    private Long courseId;

    private String keyword;

    private Status status;

    private String sortBy = "lessonOrder.asc";
}
