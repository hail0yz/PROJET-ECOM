package org.ecom.customerservice.mapper;

import org.springframework.stereotype.Component;

import org.ecom.customerservice.dto.TicketCategoryDTO;
import org.ecom.customerservice.dto.TicketDTO;
import org.ecom.customerservice.model.Ticket;
import org.ecom.customerservice.model.TicketCategory;

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

}
