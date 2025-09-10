package gr.aueb.cf.eduapp.dto;

import gr.aueb.cf.eduapp.core.enums.GenderType;
import gr.aueb.cf.eduapp.core.enums.Role;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

public record UserInsertDTO(
        @NotEmpty(message = "First name is required")
        String firstname,

        @NotEmpty(message = "Last name is required")
        String lastname,

        @Email(message = "Invalid username")
        String username,

        @Pattern(regexp = "^(?=.*?[a-z])(?=.*?[A-Z])(?=.*?\\d)(?=.*?[@#$!%&*]).{8,}$",
                message = "Invalid Password")
        String password,

        @NotEmpty(message = "VAT number is required")
        @Pattern(regexp = "\\d{9}", message = "VAT must be a 9-digit number")
        String vat,

        @NotEmpty(message = "Father's name is required")
        String fatherName,

        @NotEmpty(message = "Father's last name is required")
        String fatherLastname,

        @NotEmpty(message = "Mother's name is required")
        String motherName,

        @NotEmpty(message = "Mother's last name is required")
        String motherLastname,

        @NotEmpty(message = "Date of birth is required")
        LocalDate dateOfBirth,

        @NotEmpty(message = "Gender is required")
        GenderType gender,

        @NotEmpty(message = "Role is required")
        Role role
) {}
