package com.example.LMS.repo;

import com.example.LMS.entity.Course;
import com.example.LMS.entity.Student;
import com.example.LMS.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.lang.ScopedValue;
import java.util.List;
import java.util.Optional;

public interface CourseRepo extends JpaRepository<Course, Long> {
    boolean existsByCode(String code);
    @Query("""
    SELECT c.id from Course c
    WHERE (:name IS NULL OR LOWER(c.name) LIKE :name ESCAPE '\\')
    AND (:code IS NULL OR c.code = :code)
    AND c.status ='ACTIVE'
""")
    Page<Long> searchIds(Pageable pageable, @Param("name") String name, @Param("code") String code);


    @Query("""
    SELECT DISTINCT c FROM Course c
    LEFT JOIN FETCH c.thumbnail t ON t.status = 'ACTIVE'
    WHERE c.id IN :courseIds
""")
    List<Course> findAllByImage(@Param("courseIds") List<Long> courseIds);

    Optional<Course> findByIdAndStatus(Long id, Status status);

}
