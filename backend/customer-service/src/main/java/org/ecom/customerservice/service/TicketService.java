package org.ecom.customerservice.service;

import java.util.Map;

import jakarta.validation.ConstraintViolationException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecom.customerservice.dto.CreateTicketRequest;
import org.ecom.customerservice.dto.CreateTicketResponse;
import org.ecom.customerservice.dto.TicketCategoryDTO;
import org.ecom.customerservice.dto.TicketDTO;
import org.ecom.customerservice.exception.EntityNotFoundException;
import org.ecom.customerservice.mapper.TicketMapper;
import org.ecom.customerservice.model.Customer;
import org.ecom.customerservice.model.Ticket;
import org.ecom.customerservice.repository.CustomerRepository;
import org.ecom.customerservice.repository.TicketCategoryRepository;
import org.ecom.customerservice.repository.TicketRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class TicketService {

    private static final Map<String, String> SORT_FIELD_MAP = Map.of(
            "date", "createdAt",
            "id", "id"
    );
    private static final String ERROR_MESSAGE_SORT = "Invalid sort parameter: %s. Valid values are: %s";

    private final TicketRepository ticketRepository;

    private final TicketMapper ticketMapper;

    private final CustomerRepository customerRepository;

    private final TicketCategoryRepository ticketCategoryRepository;

    public TicketDTO getTicketById(Long customerId, Long ticketId) {
        Ticket ticket = ticketRepository.findByIdAndCustomerId(ticketId, customerId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found : " + ticketId));
        return ticketMapper.mapToTicketDTO(ticket);
    }

    public Page<TicketDTO> getCustomerTickets(Long customerId, int page, int size, String sort) {
        String property = SORT_FIELD_MAP.get(sort);
        if (property == null) {
            throw createConstraintViolation(sort);
        }

        Pageable pageRequest = PageRequest.of(page, size, Sort.Direction.DESC, property);

        return ticketRepository.findByCustomerId(customerId, pageRequest)
                .map(ticketMapper::mapToTicketDTO);
    }

    public CreateTicketResponse createTicket(Long customerId, CreateTicketRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found : " + customerId));

        Ticket ticket = Ticket.builder()
                .customer(customer)
                .subject(request.subject())
                .description(request.description())
                .build();

        Ticket saved = ticketRepository.save(ticket);

        return new CreateTicketResponse(saved.getId());
    }

    private ConstraintViolationException createConstraintViolation(String invalidSort) {
        return new ConstraintViolationException(
                String.format(ERROR_MESSAGE_SORT, invalidSort, SORT_FIELD_MAP.keySet()),
                null
        );
    }

    public Page<TicketCategoryDTO> listTicketCategories(int page, int size) {
        return ticketCategoryRepository.findAll(PageRequest.of(page, size))
                .map(ticketMapper::mapTicketCategoryToDTO);
    }

}
