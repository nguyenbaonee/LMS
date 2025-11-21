package com.example.LMS.mapper;

import com.example.LMS.dto.Request.LessonRequest;
import com.example.LMS.dto.Response.LessonResponse;
import com.example.LMS.dto.dtoProjection.LessonDTO;
import com.example.LMS.entity.Lesson;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LessonMapper {
    Lesson toLesson(LessonRequest lessonRequest);
    LessonResponse toLessonResponse(Lesson lesson);
    LessonResponse toLessonResponseFrom(LessonDTO lessonDTO);

    List<Lesson> toLessons(List<LessonRequest> lessonRequests);
    List<LessonResponse> toLessonResponses(List<Lesson> lessons);
    List<LessonDTO> toLessonResponseFroms(List<LessonDTO> lessonDTOs);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateLesson(LessonRequest lessonRequest,@MappingTarget Lesson lesson);
}
