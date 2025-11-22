package com.example.LMS.dto;

import com.example.LMS.util.I18n;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T>{

    int code;
    String message = "response.success";
    T data;

    public static <T> ApiResponse<T> ok() {
        ApiResponse<T> response = new ApiResponse<>();
        response.success();
        return response;
    }

    public ApiResponse<T> success() {
        this.code = 200;
        message = I18n.t("response.success");
        return this;
    }

    public static <T> ApiResponse<T> of(T res) {
        ApiResponse<T> response = new ApiResponse<>();
        response.data = res;
        response.success();
        return response;
    }
}
