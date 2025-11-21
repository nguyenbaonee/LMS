package com.example.LMS.dto.Response;

import com.example.LMS.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {
    Long id;

    String name;

    String email;

    List<ImageResponse> avatar;

    Status status;

}
