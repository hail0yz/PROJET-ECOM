package org.ecom.customerservice.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import static java.util.Collections.emptyList;
import org.ecom.customerservice.dto.TicketCategoryDTO;
import org.ecom.customerservice.dto.TicketDTO;
import org.ecom.customerservice.model.Ticket;
import org.ecom.customerservice.model.TicketCategory;
import org.ecom.customerservice.model.TicketMessage;

@Component
public class TicketMapper {

    public TicketDTO mapToTicketDTO(Ticket ticket) {
        if (ticket == null) {
            return null;
        }

        return TicketDTO.builder()
                .id(ticket.getId())
                .customerId(ticket.getCustomer().getId())
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .type(ticket.getType())
                .priority(ticket.getPriority())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    public TicketDTO mapToTicketDTOWithMessages(Ticket ticket) {
        if (ticket == null) {
            return null;
        }

        List<TicketDTO.Message> messages = ticket.getMessages() == null
                ? emptyList()
                : ticket.getMessages()
                    .stream()
                    .map(this::mapToMessageDTO)
                    .toList();

        return TicketDTO.builder()
                .id(ticket.getId())
                .customerId(ticket.getCustomer().getId())
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .type(ticket.getType())
                .priority(ticket.getPriority())
                .messages(messages)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    public TicketCategoryDTO mapTicketCategoryToDTO(TicketCategory category) {
        if (category == null) {
            return null;
        }

        return TicketCategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

    private TicketDTO.Message mapToMessageDTO(TicketMessage message) {
        if (message == null) {
            return null;
        }

        return TicketDTO.Message.builder()
                .id(message.getId())
                .authorId(message.getAuthorId())
                .role(message.getAuthorRole())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }

}
