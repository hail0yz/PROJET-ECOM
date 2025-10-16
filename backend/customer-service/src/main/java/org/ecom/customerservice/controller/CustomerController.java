package org.ecom.customerservice.controller;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecom.customerservice.dto.CustomerDTO;
import org.ecom.customerservice.dto.CustomerPreferencesDTO;
import org.ecom.customerservice.dto.CustomerProfileDTO;
import org.ecom.customerservice.dto.UpdatePreferencesRequest;
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

    @GetMapping("/{customerId}/profile")
    public ResponseEntity<CustomerProfileDTO> getCustomerProfile(@PathVariable String customerId) {
        return ResponseEntity.ok(customerService.getCustomerProfile(customerId));
    }

    @GetMapping("/{customerId}/preferences")
    public ResponseEntity<CustomerPreferencesDTO> getCustomerPreferences(@PathVariable String customerId) {
        return ResponseEntity.ok(customerService.getCustomerPreferences(customerId));
    }

    @PutMapping("/{id}/preferences")
    public ResponseEntity<Void> updatePreferences(
            @PathVariable String id,
            @RequestBody @Valid UpdatePreferencesRequest request
    ) {
        customerService.updatePreferences(id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<CustomerDTO>> listCustomers(
            @Parameter(name = "The current result page requested.") @RequestParam(value = "currentPage", defaultValue = DEFAULT_CURRENT_PAGE) final int page,
            @Parameter(name = "The number of results returned per page.") @RequestParam(value = "pageSize", defaultValue = DEFAULT_PAGE_SIZE) final int size
    ) {
        return ResponseEntity.ok(customerService.listCustomers(page, size));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String id) {
        // TODO customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

}
