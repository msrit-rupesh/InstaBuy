package com.example.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ForgetPasswordDTO {

    @NotNull(message = "OTP id is required")
    private long id;

    @NotBlank(message = "Username is required")
    @Email(message = "Enter the valid email")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;


}
