package com.example.LMS.repo;

import com.example.LMS.dto.dtoProjection.*;
import com.example.LMS.entity.Enrollment;
import com.example.LMS.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepo extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByIdAndStatus(Long id, Status status);

    void deleteByCourseIdInAndStudentId(List<Long> courseIds, Long studentId);

    List<Enrollment> findAllByCourseIdInAndStudentId(List<Long> courseIds, Long studentId);

    @Query("""
SELECT new com.example.LMS.dto.dtoProjection.StudentDTO(s.id, s.name, s.email,s.status)
FROM Enrollment e
JOIN e.student s
WHERE e.course.id = :courseId
AND s.status = :status
""")
    Page<StudentDTO> findStudentDTOsByCourseId(@Param("courseId") Long courseId,Status status, Pageable pageable);

    @Query("""
SELECT new com.example.LMS.dto.dtoProjection.StudentAvatarDTO(s.id, i)
FROM Student s
LEFT JOIN Image i ON i.objectId = s.id AND i.objectType = 'STUDENT' AND i.status = 'ACTIVE'
WHERE s.id IN :ids
""")
    List<StudentAvatarDTO> findStudentAvatarsByStudentIds(@Param("ids") List<Long> ids);

    @Query("""
SELECT new com.example.LMS.dto.dtoProjection.CourseDTO(
    e.course.id,
    e.course.name,
    e.course.code,
    e.course.description,
    e.course.status
)
FROM Enrollment e
WHERE e.student.id = :studentId AND e.status = :status
""")
    Page<CourseDTO> findCourseImageDTO(
            @Param("studentId") Long studentId,
            @Param("status") Status status,
            Pageable pageable
    );

    @Query("""
SELECT new com.example.LMS.dto.dtoProjection.CourseImageDTO(c.id, i)
FROM Course c
LEFT JOIN Image i ON i.objectId = c.id AND i.objectType = 'COURSE' AND i.status = 'ACTIVE'
WHERE c.id IN :courseIds
""")
    List<CourseImageDTO> findCourseImageDTOBy1(@Param("courseIds") List<Long> courseIds);

    @Query("""
SELECT new com.example.LMS.dto.dtoProjection.EnrollmentCourseDTO(
    e.id, e.enrolledAt,
    e.course.id, e.course.name, e.course.code,
    e.course.description, e.course.status
)
FROM Enrollment e
WHERE e.student.id = :studentId AND e.status = :status
""")
    Page<EnrollmentCourseDTO> findEnrollmentsWithCourse(@Param("studentId") Long studentId,
                                                        @Param("status") Status status,
                                                        Pageable pageable);

    List<Long> findByStudentIdAndStatus(java.lang.Long studentId, Status status, Pageable pageable);
}

