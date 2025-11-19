package com.example.LMS.dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentUpdate {
    @NotBlank(message = "valid.name.notnull") @Length(min = 5, max = 20, message = "valid.name.length")
    String name;

    @NotBlank(message = "valid.mail.notnull") @Email(message = "valid.mail.invalid")
    @Length( min=5, max = 100, message = "valid.mail.length")
    String email;

}
