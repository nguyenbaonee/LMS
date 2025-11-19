package com.example.LMS.mapper;

import com.example.LMS.dto.Request.CourseRequest;
import com.example.LMS.dto.Request.CourseUpdate;
import com.example.LMS.dto.Response.CourseResponse;
import com.example.LMS.dto.dtoProjection.CourseDTO;
import com.example.LMS.dto.dtoProjection.CourseImageDTO;
import com.example.LMS.entity.Course;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ImageMapper.class, EnrollmentMapper.class})
public interface CourseMapper {
    Course toCourse(CourseRequest courseRequest);
    CourseResponse toCourseResponse(Course course);
    CourseResponse toResponseFromDTO(CourseDTO courseDTO);

    List<Course> toCourses(List<CourseRequest> courseRequests);
    List<CourseResponse> toCourseResponses(List<Course> courses);
    List<CourseResponse> toResponseFromDTOs(List<CourseDTO> courseDTOs);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCourse(CourseUpdate courseUpdate, @MappingTarget Course course);
}
