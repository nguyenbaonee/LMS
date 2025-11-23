package com.example.LMS.service;

import com.example.LMS.Exception.AppException;
import com.example.LMS.Exception.ErrorCode;
import com.example.LMS.dto.ApiResponse;
import com.example.LMS.dto.Request.EnrollmentRequest;
import com.example.LMS.dto.Request.EnrollmentUpdate;
import com.example.LMS.dto.Response.CourseResponse;
import com.example.LMS.dto.Response.EnrollmentResponse;
import com.example.LMS.dto.Response.StudentResponse;
import com.example.LMS.dto.dtoProjection.*;
import com.example.LMS.entity.Course;
import com.example.LMS.entity.Enrollment;
import com.example.LMS.entity.Image;
import com.example.LMS.entity.Student;
import com.example.LMS.enums.Status;
import com.example.LMS.mapper.CourseMapper;
import com.example.LMS.mapper.EnrollmentMapper;
import com.example.LMS.mapper.StudentMap;
import com.example.LMS.repo.CourseRepo;
import com.example.LMS.repo.EnrollmentRepo;
import com.example.LMS.repo.StudentRepo;
import org.springframework.context.MessageSource;
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
    MessageSource messageSource;
    CourseMapper courseMapper;

    public EnrollmentService(EnrollmentRepo enrollmentRepo, EnrollmentMapper enrollmentMapper,
                             StudentRepo studentRepo, CourseRepo courseRepo,
                             StudentMap studentMap, MessageSource messageSource,
                             CourseMapper courseMapper) {
        this.enrollmentRepo = enrollmentRepo;
        this.enrollmentMapper = enrollmentMapper;
        this.studentRepo = studentRepo;
        this.courseRepo = courseRepo;
        this.studentMap = studentMap;
        this.messageSource = messageSource;
        this.courseMapper = courseMapper;
    }
    @Transactional(rollbackFor = Exception.class)
    public List<EnrollmentResponse> createEnrollment(EnrollmentRequest enrollmentRequest){
        Student student = studentRepo.findByIdAndStatus(enrollmentRequest.getStudentId(), Status.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENTNOTFOUND));
        List<Enrollment> enrollmentList = new ArrayList<>();

        //check courseId request ton tai khong
        List<CourseImageDTO> coursesImg = courseRepo.findCoursesWithActiveThumbnails(enrollmentRequest.getCourseIds());
        List<CourseDTO> courses = courseRepo.findCourseDTOsByIds(enrollmentRequest.getCourseIds());
        if(courses.size() != enrollmentRequest.getCourseIds().size()){
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        Map<Long, List<Image>> thumbnailsMap = coursesImg.stream()
                .collect(Collectors.groupingBy(
                        CourseImageDTO::getId,
                        Collectors.mapping(CourseImageDTO::getThumbnail, Collectors.toList())
                ));
        courses.forEach(course -> course.setThumbnail(
                thumbnailsMap.getOrDefault(course.getId(), List.of())));
        List<Course> courseList = courseMapper.toCourseFromDTOs(courses);

        List<Enrollment> enrollments = enrollmentRepo.findAllByCourseIdInAndStudentId(enrollmentRequest.getCourseIds(),enrollmentRequest.getStudentId());
        if(enrollments != null && !enrollments.isEmpty()){
            throw new AppException(ErrorCode.ENROLLMENT_EXISTS);
        }
        Boolean a = (enrollmentRequest.getCourseIds().size() == courses.size());
        courseList.forEach(course -> {
            Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .enrolledAt(LocalDateTime.now())
                .build();
            enrollmentList.add(enrollment);});
        enrollmentRepo.saveAll(enrollmentList);
        return enrollmentMapper.toEnrollmentResponses(enrollmentList);
    }
    public Page<EnrollmentResponse> getEnrollmentsByStudent(
            int page, int size, Long studentId, Status status
    ) {
        if(!studentRepo.existsByIdAndStatus(studentId, Status.ACTIVE)){
            throw new AppException(ErrorCode.STUDENTNOTFOUND);
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<EnrollmentCourseDTO> enrollmentPage =
                enrollmentRepo.findEnrollmentsWithCourse(studentId, status, pageable);

        List<Long> courseIds = enrollmentPage.getContent().stream()
                .map(e -> e.getCourse().getId())
                .toList();

        List<CourseImageDTO> courseImages = enrollmentRepo.findCourseImageDTOBy1(courseIds);
        Map<Long, List<Image>> thumbMap = courseImages.stream()
                .collect(Collectors.groupingBy(
                        CourseImageDTO::getId,
                        Collectors.mapping(CourseImageDTO::getThumbnail, Collectors.toList())
                ));

        List<EnrollmentResponse> responses = enrollmentPage.getContent().stream()
                .map(e -> {
                    CourseDTO courseDTO = e.getCourse();
                    Course course = courseMapper.toCourseFromDTO(courseDTO);
                    courseDTO.setThumbnail(thumbMap.getOrDefault(courseDTO.getId(), List.of()));

                    EnrollmentResponse resp = new EnrollmentResponse();
                    resp.setId(e.getEnrollmentId());
                    resp.setCourse(course);
                    resp.setEnrolledAt(e.getEnrolledAt());
                    return resp;
                }).toList();

        return new PageImpl<>(responses, pageable, enrollmentPage.getTotalElements());
    }
    @Transactional
    public void updateEnrollment(Long enrollmentId, EnrollmentUpdate enrollmentUpdate) {
        Enrollment enrollment = enrollmentRepo.findByIdAndStatus(enrollmentId, Status.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));
        Student student = enrollment.getStudent();
        Long studentId = student.getId();
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
    public Page<StudentResponse> getStudentsOfCourse(int page, int size, Long courseId,Status status) {
        if (!courseRepo.existsByIdAndStatus(courseId, status)) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<StudentDTO> studentDTOPage = enrollmentRepo.findStudentDTOsByCourseId(courseId, status, pageable);

        List<Long> studentIds = studentDTOPage.getContent().stream()
                .map(StudentDTO::getId)
                .collect(Collectors.toList());

        List<StudentAvatarDTO> avatars = enrollmentRepo.findStudentAvatarsByStudentIds(studentIds);

        Map<Long, List<Image>> avatarMap = avatars.stream()
                .collect(Collectors.groupingBy(
                        StudentAvatarDTO::getId,
                        Collectors.mapping(StudentAvatarDTO::getImage, Collectors.toList())
                ));

        studentDTOPage.getContent().forEach(s -> s.setAvatar(avatarMap.getOrDefault(s.getId(), List.of())));

        List<StudentResponse> studentResponseList = studentMap.toStdResponseFromDTOs(studentDTOPage.getContent());

        return new PageImpl<>(studentResponseList, pageable, studentDTOPage.getTotalElements());
    }

    @Transactional
    public ApiResponse<Void> deleteEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepo.findByIdAndStatus(enrollmentId, Status.ACTIVE)
                        .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));
        enrollmentRepo.delete(enrollment);
        return ApiResponse.<Void>builder()
                .build();
    }

}
