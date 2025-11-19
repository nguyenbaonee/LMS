package com.example.LMS.Exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    EMAILEXISTS(1001,"response.exit", HttpStatus.BAD_REQUEST),
    STUDENTNOTFOUND(1002,"response.student.notfound", HttpStatus.BAD_REQUEST),
    IMAGENOTFOUND(1003,"response.image.notfound",HttpStatus.BAD_REQUEST),
    CODECOURSEEXISTS(1004,"response.course.codeExists",HttpStatus.BAD_REQUEST),
    AVATAR_NOT_FOUND(1005,"response.student.avatarNotFound",HttpStatus.BAD_REQUEST),
    COURSE_NOT_FOUND(1007,"response.course.notFound",HttpStatus.BAD_REQUEST),
    THUMBNAIL_NOT_FOUND(1008,"response.course.ThumbnotFound",HttpStatus.BAD_REQUEST),
    VIDEO_NOT_EMPTY(1009,"response.lesson.video_not_empty",HttpStatus.BAD_REQUEST),
    LESSON_NOT_FOUND(2001,"response.lesson.not_found",HttpStatus.BAD_REQUEST),
    ENROLLMENT_EXISTS(2002,"response.enrollment.exists",HttpStatus.BAD_REQUEST),
    ENROLLMENT_NOT_FOUND(2003,"response.enrollment.not_found",HttpStatus.BAD_REQUEST);

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
    
}
