package org.ecom.customerservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateTicketMessageResponse {
    private Long messageId;
    private Long ticketId;
}
