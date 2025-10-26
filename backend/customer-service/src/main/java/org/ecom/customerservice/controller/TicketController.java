package org.ecom.customerservice.controller;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecom.customerservice.dto.CreateTicketRequest;
import org.ecom.customerservice.dto.CreateTicketResponse;
import org.ecom.customerservice.dto.TicketCategoryDTO;
import org.ecom.customerservice.dto.TicketDTO;
import org.ecom.customerservice.service.TicketService;

@RestController
@RequestMapping
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Tickets")
@Validated
public class TicketController {

    private static final String DEFAULT_PAGE_SIZE = "20";
    private static final String DEFAULT_CURRENT_PAGE = "0";

    private final TicketService ticketService;

    @GetMapping(value = "/customers/{customerId}/tickets")
    @Operation(summary = "Get all tickets for customer")
    public ResponseEntity<Page<TicketDTO>> getCustomerTickets(
            @PathVariable Long customerId,
            @Parameter(name = "The current result page requested.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int page,
            @Parameter(name = "The number of results returned per page.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int size,
            @Parameter(name = "Sorting method applied to the returned results. Currently, `date` and `id` are supported.") @RequestParam(defaultValue = "date") final String sort
    ) {
        Page<TicketDTO> tickets = ticketService.getCustomerTickets(customerId, page, size, sort);
        return ResponseEntity.ok(tickets);
    }

    @PostMapping(value = "/customers/{customerId}/tickets")
    @Operation(summary = "Create a ticket")
    public ResponseEntity<CreateTicketResponse> createTicket(
            @RequestBody @Valid CreateTicketRequest request,
            @PathVariable Long customerId
    ) {
        var response = ticketService.createTicket(customerId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping(value = "/customers/{customerId}/tickets/{ticketId}")
    @Operation(summary = "Get a ticket by ticket id.")
    public ResponseEntity<TicketDTO> getTicket(
            @PathVariable Long customerId,
            @PathVariable final Long ticketId
    ) {
        return ResponseEntity.ok(ticketService.getTicketById(customerId, ticketId)); //
    }

    @GetMapping(value = "/tickets/categories")
    @Operation(summary = "Get all ticket categories.")
    public ResponseEntity<Page<TicketCategoryDTO>> listTicketCategories(
            @Parameter(name = "The current result page requested.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int page,
            @Parameter(name = "The number of results returned per page.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int size
    ) {
        return ResponseEntity.ok(ticketService.listTicketCategories(page, size));
    }

}
