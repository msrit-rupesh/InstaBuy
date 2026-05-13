package com.example.userservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResetPasswordDTO {

    @NotNull(message = "OTP id is required")
    private long id;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "OTP is required")
    private String otp;

    @NotBlank(message = "Password is required")
    @Size(min=8 , message = "Password must be atleast of size 8")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$" ,
            message = "Password must be at least 8 characters, including uppercase, lowercase, number and special character"
    )
    private String password;

    @NotNull(message = "Role Id is required")
    private short roleId;

}

//username, otp, newPassword, roleId