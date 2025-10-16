package org.ecom.customerservice.controller;

import jakarta.validation.Valid;

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
import org.ecom.customerservice.dto.TicketCategoryListDTO;
import org.ecom.customerservice.dto.TicketDTO;
import org.ecom.customerservice.dto.TicketListDTO;

@RestController
@RequestMapping
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Tickets")
@Validated
public class TicketController {

    private static final String DEFAULT_PAGE_SIZE = "20";
    private static final String DEFAULT_CURRENT_PAGE = "0";

    @GetMapping(value = "/customers/{customerId}/tickets")
    @Operation(summary = "Get all tickets for user")
    public TicketListDTO getTickets(
            @PathVariable String customerId,
            @Parameter(name = "The current result page requested.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int page,
            @Parameter(name = "The number of results returned per page.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int size,
            @Parameter(name = "Sorting method applied to the returned results. Currently, byDate and byTicketId are supported.") @RequestParam(value = "sort", defaultValue = "byDate") final String sort
    ) {
        return null; // TODO
    }

    @PostMapping(value = "/customers/{customerId}/tickets")
    @Operation(summary = "Create a ticket")
    public TicketDTO createTicket(@RequestBody @Valid CreateTicketRequest request) {
        return null; // TODO
    }

    @GetMapping(value = "/customers/{customerId}/tickets/{ticketId}")
    @Operation(summary = "Get a ticket by ticket id.")
    public TicketDTO getTicket(
            @PathVariable final Long ticketId,
            @PathVariable String customerId
    ) {
        return null; // TODO
    }

    @GetMapping(value = "/tickets/categories")
    @Operation(summary = "Get all ticket categories.")
    public TicketCategoryListDTO getTicketCategories() {
        return null; // TODO
    }

}
