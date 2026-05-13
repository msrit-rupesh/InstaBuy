package com.example.userservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RegisterCompleteDTO {

    @NotNull(message = "OTP id is required")
    private long id;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

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
