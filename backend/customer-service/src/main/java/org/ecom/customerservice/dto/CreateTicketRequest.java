package org.ecom.customerservice.dto;

public record CreateTicketRequest(
        String subject,
        String description
) {
}
