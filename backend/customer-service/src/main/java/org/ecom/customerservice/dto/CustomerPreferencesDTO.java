package org.ecom.customerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPreferencesDTO {

    private Long id;

    private boolean emailNotificationsEnabled;

    private boolean smsNotificationsEnabled;

}
