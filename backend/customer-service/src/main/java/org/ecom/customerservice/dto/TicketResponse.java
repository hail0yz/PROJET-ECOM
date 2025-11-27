package org.ecom.customerservice.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.ecom.customerservice.model.Priority;
import org.ecom.customerservice.model.Ticket;

public class TicketResponse {
    public UUID id;
    public String customerId;
    public String subject;
    public String description;
    public Ticket.Status status;
    public Priority priority;
    public String assignedTo;
    public OffsetDateTime createdAt;
    public OffsetDateTime updatedAt;
}
