package com.example.LMS.dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public class StudentUpdate {
    @NotNull(message = "valid.name.notnull") @Length(min = 5, max = 20, message = "valid.name.length")
    String name;

    @NotNull(message = "valid.mail.notnull") @Email(message = "valid.mail.invalid")
    @Length( min=5, max = 100, message = "valid.mail.length")
    String email;

    List<ImageRequest> avatar;
}
