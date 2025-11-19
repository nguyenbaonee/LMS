package com.example.LMS.mapper;

import com.example.LMS.dto.Request.StudentRequest;
import com.example.LMS.dto.Request.StudentUpdate;
import com.example.LMS.dto.Response.StudentResponse;
import com.example.LMS.dto.dtoProjection.StudentDTO;
import com.example.LMS.entity.Student;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ImageMapper.class, EnrollmentMapper.class})
public interface StudentMap {
    Student toStudent(StudentRequest studentRequest);
    StudentResponse toStdResponse(Student student);
    StudentResponse toStdResponseFromDTO(StudentDTO studentDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateStudent(StudentUpdate studentUpdate, @MappingTarget Student student);

    List<Student> toStudents(List<StudentRequest> studentRequestList);
    List<StudentResponse> toStdResponses(List<Student> students);
    List<StudentResponse> toStdResponseFromDTOs (List<StudentDTO> studentDTOList);
}
