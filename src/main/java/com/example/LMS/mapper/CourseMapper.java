package com.example.LMS.mapper;

import com.example.LMS.dto.Request.CourseRequest;
import com.example.LMS.dto.Request.CourseUpdate;
import com.example.LMS.dto.Response.CourseResponse;
import com.example.LMS.entity.Course;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CourseMapper {
    Course toCourse(CourseRequest courseRequest);
    CourseResponse toCourseResponse(Course course);

    List<Course> toCourses(List<CourseRequest> courseRequests);
    List<CourseResponse> toCourseResponses(List<Course> courses);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCourse(CourseUpdate courseUpdate, @MappingTarget Course course);
}
