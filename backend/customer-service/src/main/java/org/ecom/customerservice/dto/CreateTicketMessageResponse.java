package org.ecom.customerservice.dto;

import lombok.Builder;

@Builder
public class CreateTicketMessageResponse {
    private Long messageId;
    private Long ticketId;
}
