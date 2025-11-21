package com.example.LMS.repo.extend;

import com.example.LMS.dto.Request.LessonQuery;
import com.example.LMS.dto.dtoProjection.LessonDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LessonRepoExtend {
    List<LessonDTO> search(LessonQuery query, Pageable pageable);

    Long count(LessonQuery query);
}
