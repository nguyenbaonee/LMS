package com.example.LMS.repo;

import com.example.LMS.dto.dtoProjection.CourseDTO;
import com.example.LMS.dto.dtoProjection.CourseImageDTO;
import com.example.LMS.entity.Course;
import com.example.LMS.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepo extends JpaRepository<Course, Long> {
    boolean existsByCode(String code);

    //page<long> CourseIds dung dieu kien search
    @Query("""
    SELECT c.id from Course c
    WHERE (:name IS NULL OR LOWER(c.name) LIKE :name ESCAPE '\\')
    AND (:code IS NULL OR c.code = :code)
    AND c.status =:status
""")
    Page<Long> searchIds(Pageable pageable, @Param("name") String name, @Param("code") String code, @Param ("status") Status status);

    //list cac Course co trong CourseIds
    @Query("""
SELECT new com.example.LMS.dto.dtoProjection.CourseImageDTO(c.id,i)
FROM Course c
LEFT JOIN Image i ON i.objectId = c.id AND i.objectType = 'COURSE' AND i.status = 'ACTIVE'
WHERE c.id IN :courseIds
""")
    List<CourseImageDTO> findCoursesWithActiveThumbnails(@Param("courseIds") List<Long> courseIds);

    @Query("""
SELECT new com.example.LMS.dto.dtoProjection.CourseDTO(
    c.id, c.name, c.code, c.description,c.status
)
FROM Course c
WHERE c.id IN :courseIds
AND c.status = :status
""")
    List<CourseDTO> findCourseDTOsByIds1(@Param("courseIds") List<Long> courseIds, @Param("status") Status status);
    @Query("""
SELECT new com.example.LMS.dto.dtoProjection.CourseDTO(
    c.id, c.name, c.code, c.description, c.status
)
FROM Course c
WHERE c.id IN :courseIds
AND c.status ='ACTIVE'
""")
    List<CourseDTO> findCourseDTOsByIds(@Param("courseIds") List<Long> courseIds);
    Optional<Course> findByIdAndStatus(Long id, Status status);
    boolean existsByIdAndStatus(Long id, Status status);


    List<Course> findAllByIdInAndStatus(List<Long> courseIds, Status status);
}
