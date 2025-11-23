package com.example.LMS.repo.extend;

import com.example.LMS.dto.Request.CourseQuery;
import com.example.LMS.dto.dtoProjection.CourseDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CourseRepoExtend {
    List<CourseDTO> search(CourseQuery query, Pageable pageable);

    Long count(CourseQuery query);
}