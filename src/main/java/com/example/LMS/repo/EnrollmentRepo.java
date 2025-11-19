package com.example.LMS.repo;

import com.example.LMS.dto.dtoProjection.StudentAvatarDTO;
import com.example.LMS.dto.dtoProjection.StudentDTO;
import com.example.LMS.entity.Enrollment;
import com.example.LMS.enums.Status;
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
SELECT new com.example.LMS.dto.dtoProjection.StudentAvatarDTO(s.id, i)
FROM Enrollment e
LEFT JOIN Image i ON i.objectId = e.id AND i.objectType = 'STUDENT' AND i.status = 'ACTIVE'
LEFT JOIN Student s ON s.id = e.student.id AND s.status = 'ACTIVE'
WHERE e.course.id = :courseId
""")
    List<StudentAvatarDTO> findStudentAvatars(@Param("courseId") Long courseId);

    @Query("""
SELECT new com.example.LMS.dto.dtoProjection.StudentDTO(
    s.id, s.name, s.email
)
FROM Student s
WHERE s.id = :id
AND s.status = 'ACTIVE'
""")
    List<StudentDTO> findStudentDTOsByIds(@Param("id") Long id);
}
