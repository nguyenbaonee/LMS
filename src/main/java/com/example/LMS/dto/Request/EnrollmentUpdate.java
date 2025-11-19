package com.example.LMS.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentUpdate {
    List<Long> newCourseIds;
    List<Long> deleteCourseIds;
}
