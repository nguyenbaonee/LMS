package com.example.LMS.repo;

import com.example.LMS.dto.dtoProjection.CourseDTO;
import com.example.LMS.dto.dtoProjection.StudentAvatarDTO;
import com.example.LMS.dto.dtoProjection.StudentDTO;
import com.example.LMS.entity.Student;
import com.example.LMS.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepo extends JpaRepository<Student, Long> {
    boolean existsByEmail(String email);
    @Query("""
    SELECT s.id from Student s
    WHERE (:name IS NULL OR LOWER(s.name) LIKE :name ESCAPE '\\')
    AND (:email IS NULL OR s.email = :email)
    AND s.status ='ACTIVE'
""")
    Page<Long> searchIds(Pageable pageable, @Param("name") String name,@Param("email") String email);

    @Query("""
SELECT new com.example.LMS.dto.dtoProjection.StudentAvatarDTO(s.id, i)
FROM Student s
LEFT JOIN Image i ON i.objectId = s.id AND i.objectType = 'STUDENT' AND i.status = 'ACTIVE'
WHERE s.id IN :ids
""")
    List<StudentAvatarDTO> findStudentAvatars(@Param("ids") List<Long> ids);

    @Query("""
SELECT new com.example.LMS.dto.dtoProjection.StudentDTO(
    s.id, s.name, s.email
)
FROM Student s
WHERE s.id IN :ids
AND s.status = 'ACTIVE'
""")
    List<StudentDTO> findStudentDTOsByIds(@Param("ids") List<Long> ids);

    Optional<Student> findByIdAndStatus(Long id, Status status);

    @Query("SELECT s.name FROM Student s WHERE s.id = :id")
    String findNameById(@Param("id") Long id);

    @Query("SELECT s.email FROM Student s WHERE s.id = :id")
    String findEmailById(@Param("id") Long id);
}
