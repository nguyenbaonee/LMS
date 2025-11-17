package com.example.LMS.Exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    EMAILEXISTS(1001,"response.exit", HttpStatus.BAD_REQUEST),
    STUDENTNOTFOUND(1002,"response.student.notfound", HttpStatus.BAD_REQUEST),
    IMAGENOTFOUND(1003,"response.image.notfound",HttpStatus.BAD_REQUEST);

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
    
}
