package org.ecom.customerservice.controller;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecom.customerservice.dto.CustomerDTO;
import org.ecom.customerservice.dto.CustomerDetailsDTO;
import org.ecom.customerservice.dto.CustomerPreferencesDTO;
import org.ecom.customerservice.dto.CustomerProfileDTO;
import org.ecom.customerservice.dto.CustomerStatsResponse;
import org.ecom.customerservice.dto.TicketStatsResponse;
import org.ecom.customerservice.dto.UpdatePreferencesRequest;
import org.ecom.customerservice.dto.UpdateProfileRequest;
import org.ecom.customerservice.service.CustomerService;

@RestController
@RequestMapping("/api/v1/customers")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Customers")
@Validated
public class CustomerController {

    private static final String DEFAULT_PAGE_SIZE = "20";
    private static final String DEFAULT_CURRENT_PAGE = "0";

    private final CustomerService customerService;

    @Operation(summary = "Get customer profile by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer profile retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PreAuthorize("hasAuthority('ROLE_ADMIN') || (hasAuthority('ROLE_USER') && @customerService.canAccessCustomerProfile(#customerId))")
    @GetMapping("/{customerId}/profile")
    public ResponseEntity<CustomerProfileDTO> getCustomerProfile(@PathVariable String customerId) {
        return ResponseEntity.ok(customerService.getCustomerProfile(customerId));
    }

    @Operation(summary = "Update customer profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer profile updated successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("hasAuthority('ROLE_ADMIN') || (hasAuthority('ROLE_USER') && @customerService.canAccessCustomerProfile(#customerId))")
    @PutMapping("/{customerId}/profile")
    public ResponseEntity<CustomerProfileDTO> updateCustomerProfile(
            @Parameter(description = "ID of the customer") @PathVariable String customerId,
            @RequestBody @Valid UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(customerService.updateCustomerProfile(customerId, request));
    }

    @Operation(summary = "Get customer details by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PreAuthorize("hasAuthority('ROLE_ADMIN') || (hasAuthority('ROLE_USER') && @customerService.canAccessCustomerProfile(#customerId))")
    @GetMapping("/{customerId}/details")
    public ResponseEntity<CustomerDetailsDTO> getCustomerDetails(@PathVariable String customerId) {
        return ResponseEntity.ok(customerService.getCustomerDetails(customerId));
    }

    @Operation(summary = "Get customer preferences by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer preferences retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PreAuthorize("hasAuthority('ROLE_ADMIN') || (hasAuthority('ROLE_USER') && @customerService.canAccessCustomerProfile(#customerId))")
    @GetMapping("/{customerId}/preferences")
    public ResponseEntity<CustomerPreferencesDTO> getCustomerPreferences(
            @Parameter(description = "ID of the customer") @PathVariable String customerId
    ) {
        return ResponseEntity.ok(customerService.getCustomerPreferences(customerId));
    }

    @Operation(summary = "Update customer preferences")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer preferences updated successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PreAuthorize("hasAuthority('ROLE_ADMIN') || (hasAuthority('ROLE_USER') && @customerService.canAccessCustomerProfile(#id))")
    @PutMapping("/{id}/preferences")
    public ResponseEntity<Void> updatePreferences(
            @Parameter(description = "ID of the customer") @PathVariable String id,
            @RequestBody @Valid UpdatePreferencesRequest request
    ) {
        customerService.updateCustomerPreferences(id, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "List customers with pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customers retrieved successfully")
    })
    @GetMapping(path = {"", "/"})
    public ResponseEntity<Page<CustomerDTO>> listCustomers(
            @Parameter(name = "The current result page requested.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int page,
            @Parameter(name = "The number of results returned per page.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int size
    ) {
        return ResponseEntity.ok(customerService.listCustomers(page, size));
    }

    @Operation(summary = "Delete a customer by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "ID of the customer") @PathVariable String id
    ) {
        // TODO customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @Operation(summary = "Get customer statistics (admin/support only)")
    public ResponseEntity<CustomerStatsResponse> getCustomerStats() {
        CustomerStatsResponse stats = customerService.getCustomerStats();
        return ResponseEntity.ok(stats);
    }

}
