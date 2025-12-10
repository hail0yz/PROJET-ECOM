package org.ecom.customerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Builder;
import org.ecom.customerservice.model.Priority;
import org.ecom.customerservice.model.Ticket;

@Builder
public record CreateTicketRequest(
        @NotBlank String subject,
        @NotBlank String description,
        Priority priority,
        @NotNull Ticket.Type type
) {
}
