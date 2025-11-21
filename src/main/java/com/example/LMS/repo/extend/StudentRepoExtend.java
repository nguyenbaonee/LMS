package com.example.LMS.repo.extend;

import com.example.LMS.dto.Request.StudentQuery;
import com.example.LMS.dto.dtoProjection.StudentDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudentRepoExtend {
    List<StudentDTO> search(StudentQuery query, Pageable pageable);

    Long count(StudentQuery query);
}
