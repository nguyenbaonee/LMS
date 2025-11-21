package com.example.LMS.repo;

import com.example.LMS.dto.dtoProjection.LessonThumbDTO;
import com.example.LMS.entity.Lesson;
import com.example.LMS.enums.Status;
import com.example.LMS.repo.extend.LessonRepoExtend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonRepo extends JpaRepository<Lesson, Long>, LessonRepoExtend {
    List<Lesson> findByCourseIdAndStatusOrderByLessonOrder(Long courseId, Status status);

    @Query("""
SELECT new com.example.LMS.dto.dtoProjection.LessonThumbDTO(l.id, i)
FROM Lesson l
LEFT JOIN Image i ON i.objectId = l.id AND i.objectType = 'LESSON' AND i.status = 'ACTIVE'
WHERE l.id IN :ids
""")
    List<LessonThumbDTO> findLessonThumb(@Param("ids") List<Long> ids);

    Optional<Lesson> findByIdAndStatus(Long lessonId, Status status);
}
