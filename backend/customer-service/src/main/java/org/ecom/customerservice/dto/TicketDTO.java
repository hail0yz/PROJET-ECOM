package org.ecom.customerservice.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ecom.customerservice.model.Priority;
import org.ecom.customerservice.model.Ticket;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDTO {

    private Long id;

    private String customerId;

    private String subject;

    private String description;

    private Ticket.Status status;

    private Priority priority;
    
    private Ticket.Type type;

    private List<Message> messages;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {

        private Long id;

        private String authorId;
        
        private String role;

        private String content;

        private LocalDateTime createdAt;

    }

}
