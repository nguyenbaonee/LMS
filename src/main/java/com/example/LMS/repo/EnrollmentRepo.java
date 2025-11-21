package com.example.LMS.repo;

import com.example.LMS.dto.dtoProjection.StudentAvatarDTO;
import com.example.LMS.dto.dtoProjection.StudentDTO;
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
AND s.status = 'ACTIVE'
""")
    Page<StudentDTO> findStudentDTOsByCourseId(@Param("courseId") Long courseId, Pageable pageable);

    @Query("""
SELECT new com.example.LMS.dto.dtoProjection.StudentAvatarDTO(s.id, i)
FROM Student s
LEFT JOIN Image i ON i.objectId = s.id AND i.objectType = 'STUDENT' AND i.status = 'ACTIVE'
WHERE s.id IN :ids
""")
    List<StudentAvatarDTO> findStudentAvatarsByStudentIds(@Param("ids") List<Long> ids);

}
