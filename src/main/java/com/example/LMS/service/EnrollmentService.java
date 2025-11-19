package com.example.LMS.service;

import com.example.LMS.Exception.AppException;
import com.example.LMS.Exception.ErrorCode;
import com.example.LMS.dto.Request.EnrollmentRequest;
import com.example.LMS.dto.Request.EnrollmentUpdate;
import com.example.LMS.dto.Response.EnrollmentResponse;
import com.example.LMS.dto.Response.StudentResponse;
import com.example.LMS.dto.dtoProjection.StudentAvatarDTO;
import com.example.LMS.dto.dtoProjection.StudentDTO;
import com.example.LMS.entity.Course;
import com.example.LMS.entity.Enrollment;
import com.example.LMS.entity.Image;
import com.example.LMS.entity.Student;
import com.example.LMS.enums.Status;
import com.example.LMS.mapper.EnrollmentMapper;
import com.example.LMS.mapper.StudentMap;
import com.example.LMS.repo.CourseRepo;
import com.example.LMS.repo.EnrollmentRepo;
import com.example.LMS.repo.StudentRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {
    EnrollmentRepo enrollmentRepo;
    EnrollmentMapper enrollmentMapper;
    StudentRepo studentRepo;
    CourseRepo courseRepo;
    StudentMap studentMap;

    public EnrollmentService(EnrollmentRepo enrollmentRepo, EnrollmentMapper enrollmentMapper,
                             StudentRepo studentRepo, CourseRepo courseRepo,
                             StudentMap studentMap) {
        this.enrollmentRepo = enrollmentRepo;
        this.enrollmentMapper = enrollmentMapper;
        this.studentRepo = studentRepo;
        this.courseRepo = courseRepo;
        this.studentMap = studentMap;
    }
    @Transactional(rollbackFor = Exception.class)
    public List<EnrollmentResponse> createEnrollment(EnrollmentRequest enrollmentRequest){
        Student student = studentRepo.findByIdAndStatus(enrollmentRequest.getStudentId(), Status.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENTNOTFOUND));
        List<Enrollment> enrollmentList = new ArrayList<>();

        //check courseId request ton tai khong
        List<Course> courses = courseRepo.findAllByIdInAndStatus(enrollmentRequest.getCourseIds(), Status.ACTIVE);
        if(courses.size()!= enrollmentRequest.getCourseIds().size()){
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        Map<Long, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getId, c -> c));

        List<Enrollment> enrollments = enrollmentRepo.findAllByCourseIdInAndStudentId(enrollmentRequest.getCourseIds(),enrollmentRequest.getStudentId());
        if(enrollments != null && !enrollments.isEmpty()){
            throw new AppException(ErrorCode.ENROLLMENT_EXISTS);
        }
        for(Long courseId : enrollmentRequest.getCourseIds()){
            Enrollment enrollment = Enrollment.builder()
                    .student(student)
                    .course(courseMap.get(courseId))
                    .enrolledAt(LocalDateTime.now())
                    .build();
            enrollmentList.add(enrollment);
        }
        enrollmentRepo.saveAll(enrollmentList);
        return enrollmentMapper.toEnrollmentResponses(enrollmentList);
    }
    @Transactional
    public void updateEnrollment(Long studentId, EnrollmentUpdate enrollmentUpdate) {
        Student student = studentRepo.findByIdAndStatus(studentId, Status.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENTNOTFOUND));
        List<Long> newCourseIds = enrollmentUpdate.getNewCourseIds();
        List<Long> deleteCourseIds = enrollmentUpdate.getDeleteCourseIds();

        //list cac course xoa
        if(deleteCourseIds != null && !deleteCourseIds.isEmpty()){

            List<Enrollment> enrollments = enrollmentRepo.findAllByCourseIdInAndStudentId(deleteCourseIds,studentId);
            Set<Long> enrolledCourseIds = enrollments.stream()
                    .map(e -> e.getCourse().getId())
                    .collect(Collectors.toSet());

            for (Long id : deleteCourseIds) {
                if (!enrolledCourseIds.contains(id)) {
                    throw new AppException(ErrorCode.ENROLLMENT_NOT_FOUND);
                }
            }
            enrollmentRepo.deleteByCourseIdInAndStudentId(deleteCourseIds, studentId);
        }

        //list cac courst add
        if(newCourseIds != null && !newCourseIds.isEmpty()){
            List<Course> courseListNew = courseRepo.findAllByIdInAndStatus(newCourseIds,Status.ACTIVE);
            if(newCourseIds.size() != courseListNew.size()){
                throw new AppException(ErrorCode.COURSE_NOT_FOUND);
            }
            Map<Long, Course> courseMap = courseListNew.stream()
                    .collect(Collectors.toMap(Course::getId, c -> c));

            List<Enrollment> enrollments = enrollmentRepo.findAllByCourseIdInAndStudentId(newCourseIds,studentId);


            Set<Long> alreadyEnrolled = enrollments.stream()
                    .map(e -> e.getCourse().getId())
                    .collect(Collectors.toSet());

            List<Long> idsToAdd = newCourseIds.stream()
                    .filter(id -> !alreadyEnrolled.contains(id))
                    .toList();

            List<Enrollment> toSave = new ArrayList<>();
            for(Long id : idsToAdd){
                toSave.add(
                        Enrollment.builder()
                                .student(student)
                                .course(courseMap.get(id))
                                .enrolledAt(LocalDateTime.now())
                                .build()
                );
            }
            enrollmentRepo.saveAll(toSave);

        }
    }
    public Page<StudentResponse> getStudentsOfCourse(int page, int size, Long courseId) {
        if(!courseRepo.existsByIdAndStatus(courseId, Status.ACTIVE)){
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        Pageable pageable = PageRequest.of(page, size);
        //dto tung student-imag
        List<StudentAvatarDTO> studentAvatarDTOS = enrollmentRepo.findStudentAvatars(courseId);
        //map id-list<image>
        Map<Long, List<Image>> studentsMap = studentAvatarDTOS.stream()
                .collect(Collectors.groupingBy(StudentAvatarDTO::getId, Collectors.mapping(StudentAvatarDTO::getImage,
                        Collectors.toList())));
        List<StudentDTO> studentDTOList = enrollmentRepo.findStudentDTOsByIds(courseId);
        studentDTOList.forEach(studentDTO -> {studentDTO
                .setAvatar(studentsMap.getOrDefault(studentDTO.getId(), List.of()));});
        List<StudentResponse> studentResponseList = studentMap.toStdResponseFromDTOs(studentDTOList);
        return new PageImpl<>(studentResponseList, pageable, studentResponseList.size());
    }
    @Transactional
    public void deleteEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepo.findByIdAndStatus(enrollmentId, Status.ACTIVE)
                        .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));
        enrollmentRepo.delete(enrollment);
    }

}
