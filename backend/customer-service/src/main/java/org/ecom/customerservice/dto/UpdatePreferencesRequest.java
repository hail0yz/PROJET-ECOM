package org.ecom.customerservice.dto;

public record UpdatePreferencesRequest(
    boolean emailNotificationsEnabled,
    boolean smsNotificationsEnabled
) {
}
