package org.ecom.customerservice.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {

    private Long id;

    private String externalId;

    private String name;

    private String firstname;

    private String lastname;

    private String email;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
