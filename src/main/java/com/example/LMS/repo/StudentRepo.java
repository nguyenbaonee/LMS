package com.example.LMS.repo;

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

public interface StudentRepo extends JpaRepository<Student, Long> {
    boolean existsByEmail(String email);
    @Query("""
    SELECT s.id from Student s
    WHERE (:name IS NULL OR LOWER(s.name) LIKE :name ESCAPE '\\')
    AND (:email IS NULL OR s.email = :email)
    AND s.status ='1'
""")
    Page<Long> searchIds(Pageable pageable, @Param("name") String name,@Param("email") String email);

    @Query("""
    SELECT DISTINCT s FROM Student s
    LEFT JOIN FETCH s.avatar a ON a.status = '1'
    LEFT JOIN FETCH s.enrollments e ON e.status = '1'
    WHERE s.id IN :studentIds
""")
    List<Student> findAllByImageAndEnrollments(@Param("studentIds") List<Long> studentIds);

    Optional<Student> findByIdAndStatus(Long id, Status status);
}
