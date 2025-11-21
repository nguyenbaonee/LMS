package com.example.LMS.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T>{
    MessageSource messageSource;
    public ApiResponse(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    int code;
    @Builder.Default
    String message = "response.delete.success";
    T data;
}
