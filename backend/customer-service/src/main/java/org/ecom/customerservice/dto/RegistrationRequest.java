package org.ecom.customerservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegistrationRequest(
        @NotBlank String firstname,
        @NotBlank String lastname,
        @Email String email,
        @NotBlank String password
) {
}
