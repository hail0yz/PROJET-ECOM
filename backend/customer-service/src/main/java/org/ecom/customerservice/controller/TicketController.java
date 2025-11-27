package org.ecom.customerservice.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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
import org.ecom.customerservice.AuthorizationUtils;
import org.ecom.customerservice.dto.CreateTicketMessageRequest;
import org.ecom.customerservice.dto.CreateTicketMessageResponse;
import org.ecom.customerservice.dto.CreateTicketRequest;
import org.ecom.customerservice.dto.CreateTicketResponse;
import org.ecom.customerservice.dto.TicketCategoryDTO;
import org.ecom.customerservice.dto.TicketDTO;
import org.ecom.customerservice.dto.TicketStatsResponse;
import org.ecom.customerservice.service.TicketService;

@RestController
@RequestMapping("/api/v1/tickets")
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
            @PathVariable String customerId,
            @Parameter(name = "The current result page requested.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int page,
            @Parameter(name = "The number of results returned per page.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int size,
            @Parameter(name = "Sorting method applied to the returned results. Currently, `date` and `id` are supported.") @RequestParam(defaultValue = "date") final String sort
    ) {
        Page<TicketDTO> tickets = ticketService.getCustomerTickets(customerId, page, size, sort);
        return ResponseEntity.ok(tickets);
    }

    @PostMapping(path = {"", "/"})
    @Operation(summary = "Create a ticket")
    public ResponseEntity<CreateTicketResponse> createTicket(@RequestBody @Valid CreateTicketRequest request,
                                                             @AuthenticationPrincipal(expression = "subject") String customerId) {
        var response = ticketService.createTicket(customerId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping(value = "/customers/{customerId}/tickets/{ticketId}")
    @Operation(summary = "Get a ticket by ticket id.")
    public ResponseEntity<TicketDTO> getTicket(
            @PathVariable String customerId,
            @PathVariable final Long ticketId
    ) {
        return ResponseEntity.ok(ticketService.getTicketById(customerId, ticketId)); //
    }

    @GetMapping(path = {"", "/"})
    public ResponseEntity<Page<TicketDTO>> listAllTickets(@RequestParam(name = "page", defaultValue = "0") int page,
                                                               @RequestParam(name = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(ticketService.listTickets(page, size));
    }

    @GetMapping("/me")
    public ResponseEntity<Page<TicketDTO>> listMyTickets(@RequestParam(name = "page", defaultValue = "0") int page,
                                                         @RequestParam(name = "size", defaultValue = "20") int size,
                                                         @AuthenticationPrincipal(expression = "subject") String customerId) {
        return ResponseEntity.ok(ticketService.listCustomerTickets(customerId, page, size));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPPORT')")
    @Operation(summary = "Get ticket statistics (admin/support only)")
    public ResponseEntity<TicketStatsResponse> getTicketStats() {
        TicketStatsResponse stats = ticketService.getTicketStats();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/{id}/messages")
    @PreAuthorize("@ticketService.canAccessTicket(authentication, #userId, #id)")
    public ResponseEntity<CreateTicketMessageResponse> addMessage(@PathVariable("id") Long id,
                                                                  @RequestBody @Valid CreateTicketMessageRequest request,
                                                                  @AuthenticationPrincipal(expression = "subject") String userId) {
        List<String> roles = AuthorizationUtils.getAuthorities(SecurityContextHolder.getContext().getAuthentication());

        CreateTicketMessageResponse response = ticketService.addMessage(id, request, userId, roles);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

//    TODO implement status change
//    @PostMapping("/{id}/status")
//    @PreAuthorize("hasRole('SUPPORT') or hasRole('ADMIN')")
//    public ResponseEntity<Ticket> changeStatus(@PathVariable("id") UUID id, @RequestParam("status") TicketStatus status) {
//        Ticket t = ticketService.changeStatus(id, status);
//        return ResponseEntity.ok(t);
//    }

    // TODO implement priority change
//    @PostMapping("/{id}/assign")
//    @PreAuthorize("hasRole('SUPPORT') or hasRole('ADMIN')")
//    public ResponseEntity<Ticket> assign(@PathVariable("id") UUID id, @RequestParam("userId") String userId) {
//        Ticket t = ticketService.assignTo(id, userId);
//        return ResponseEntity.ok(t);
//    }

}
