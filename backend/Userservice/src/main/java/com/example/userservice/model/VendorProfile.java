package com.example.userservice.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "vendor_profiles",
        indexes = {
                @Index(name = "idx__vendor_profiles_user_id",columnList = "user_id")
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VendorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id",nullable = false)
    @NotNull(message = "User id is Required")
    private Long userId;

    @Column(name = "company_name", nullable = false)
    @NotBlank(message = "Company name is required")
    private String companyName;

    @Column(nullable = false)
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Phone number must be a valid 10-digit Indian mobile number"
    )
    private String phone;

    @Column(name = "street_address", nullable = false)
    @NotBlank(message = "Street address is required")
    private String streetAddress;

    @Column(nullable = false)
    @NotBlank(message = "City is required")
    private String city;

    @Column(nullable = false)
    @NotBlank(message = "State is required")
    private String state;

    @Column(nullable = false)
    @NotBlank(message = "Country is required")
    private String country;

    @Column(name = "postal_code", nullable = false)
    @NotBlank(message = "Postal code is required")
    private String postalCode;

}
