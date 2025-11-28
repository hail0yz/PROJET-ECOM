package org.ecom.customerservice.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.ecom.customerservice.dto.TicketCategoryDTO;
import org.ecom.customerservice.dto.TicketDTO;
import org.ecom.customerservice.model.Customer;
import org.ecom.customerservice.model.Ticket;
import org.ecom.customerservice.model.TicketCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TicketMapperTest {

    private TicketMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TicketMapper();
    }

    @Test
    void mapToTicketDTO_nullInput_returnsNull() {
        TicketDTO dto = mapper.mapToTicketDTO(null);
        assertNull(dto);
    }

    @Test
    void mapToTicketDTO_populated_mapsCorrectly() {
        Customer customer = Customer.builder()
                .id("cust-1")
                .build();

        Ticket ticket = Ticket.builder()
                .id(10L)
                .customer(customer)
                .subject("Help")
                .description("Please help")
                .createdAt(LocalDateTime.now())
                .build();

        TicketDTO dto = mapper.mapToTicketDTO(ticket);

        assertEquals(10L, dto.getId());
        assertEquals("cust-1", dto.getCustomerId());
        assertEquals("Help", dto.getSubject());
        assertEquals("Please help", dto.getDescription());
    }

    @Test
    void mapTicketCategoryToDTO_nullInput_returnsNull() {
        TicketCategoryDTO dto = mapper.mapTicketCategoryToDTO(null);
        assertNull(dto);
    }

    @Test
    void mapTicketCategoryToDTO_populated_mapsCorrectly() {
        TicketCategory category = TicketCategory.builder()
                .id(5L)
                .name("Billing")
                .description("Billing issues")
                .build();

        TicketCategoryDTO dto = mapper.mapTicketCategoryToDTO(category);

        assertEquals(5L, dto.getId());
        assertEquals("Billing", dto.getName());
        assertEquals("Billing issues", dto.getDescription());
    }
}
