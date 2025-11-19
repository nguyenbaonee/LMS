package com.example.LMS.repo;

import com.example.LMS.entity.Lesson;
import com.example.LMS.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonRepo extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourseIdAndStatusOrderByLessonOrder(Long courseId, Status status);
    Page<Lesson> findByCourseId(Long courseId, Pageable pageable);

    Optional<Lesson> findByIdAndStatus(Long lessonId, Status status);
}
