package org.ecom.customerservice.model;

import jakarta.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Preferences {

    private boolean emailNotificationsEnabled;

    private boolean smsNotificationsEnabled;

}
