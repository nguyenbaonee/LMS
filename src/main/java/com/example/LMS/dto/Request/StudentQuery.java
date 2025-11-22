package com.example.LMS.dto.Request;

import com.example.LMS.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentQuery extends SearchQuery{
    private String keyword;

    private Status status;

    private String sortBy;

    private Long courseId;
}