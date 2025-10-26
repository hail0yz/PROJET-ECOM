package org.ecom.customerservice.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileDTO {

    private Long id;

    private String email;

    private String firstname;

    private String lastname;

    private String name;

    private String avatar;

    private LocalDate dateOfBirth;

    private String phoneNumber;

}
