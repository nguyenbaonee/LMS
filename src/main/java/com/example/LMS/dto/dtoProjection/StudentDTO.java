package com.example.LMS.dto.dtoProjection;

import com.example.LMS.entity.Image;
import com.example.LMS.entity.Student;
import com.example.LMS.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {
    Long id;

    String name;

    String email;

    List<Image> avatar;
    Status status;

    public StudentDTO(Long id, String name, String email, Status status) {
        this.id=id;
        this.name = name;
        this.email = email;
        this.status = status;
    }

}
