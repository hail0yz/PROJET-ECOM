package org.ecom.customerservice.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecom.customerservice.dto.CustomerDTO;
import org.ecom.customerservice.dto.CustomerDetailsDTO;
import org.ecom.customerservice.dto.CustomerPreferencesDTO;
import org.ecom.customerservice.dto.CustomerProfileDTO;
import org.ecom.customerservice.dto.CustomerStatsResponse;
import org.ecom.customerservice.dto.UpdatePreferencesRequest;
import org.ecom.customerservice.dto.UpdateProfileRequest;
import org.ecom.customerservice.exception.EntityNotFoundException;
import org.ecom.customerservice.mapper.CustomerMapper;
import org.ecom.customerservice.model.Contact;
import org.ecom.customerservice.model.Customer;
import org.ecom.customerservice.model.PhoneNumber;
import org.ecom.customerservice.model.Preferences;
import org.ecom.customerservice.repository.CustomerRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    private final CustomerMapper customerMapper;

    public boolean canAccessCustomerProfile(String customerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            String authenticatedUserId = jwt.getSubject();
            boolean canAccess = authenticatedUserId != null && authenticatedUserId.equals(customerId);
            log.debug("Access check for customer {}: authenticated user {}, canAccess: {}",
                    customerId, authenticatedUserId, canAccess);
            return canAccess;
        }

        return false;
    }

    public CustomerDTO getCustomerById(String customerId) {
        Customer customer = findCustomerById(customerId);
        return customerMapper.mapToCustomerDTO(customer);
    }

    public CustomerProfileDTO getCustomerProfile(String customerId) {
        Customer customer = findCustomerById(customerId);
        return customerMapper.mapToCustomerProfileDTO(customer);
    }

    public CustomerProfileDTO updateCustomerProfile(String customerId, UpdateProfileRequest request) {
        Customer customer = findCustomerById(customerId);

        customer.setFirstname(request.firstname());
        customer.setLastname(request.lastname());

        if (request.phone() != null && !request.phone().isBlank()) {
            String email = Optional.ofNullable(customer.getContact())
                    .map(Contact::getEmail)
                    .orElse(null);
            customer.setContact(new Contact(email, new PhoneNumber(null, request.phone())));
        }
        else {
            customer.setContact(new Contact(email, new PhoneNumber(null, null)));
        }

        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Customer profile updated: {}", customerId);

        return customerMapper.mapToCustomerProfileDTO(updatedCustomer);
    }

    public void updateCustomerPreferences(String customerId, UpdatePreferencesRequest request) {
        log.info("Updating preferences for customer {}: {}", customerId, request);

        Customer customer = findCustomerById(customerId);

        if (customer.getPreferences() == null) {
            log.info("No existing preferences found for customer {}. Creating new preferences.", customerId);
            Preferences preferences = Preferences.builder()
                    .emailNotificationsEnabled(request.emailNotificationsEnabled())
                    .smsNotificationsEnabled(request.smsNotificationsEnabled())
                    .build();
            customer.setPreferences(preferences);
        }
        else {
            customer.getPreferences().setEmailNotificationsEnabled(request.emailNotificationsEnabled());
            customer.getPreferences().setSmsNotificationsEnabled(request.smsNotificationsEnabled());
        }

        customerRepository.save(customer);
    }

    public CustomerPreferencesDTO getCustomerPreferences(String customerId) {
        Customer customer = findCustomerById(customerId);
        return customerMapper.mapToCustomerPreferencesDTO(customer);
    }

    public Page<CustomerDTO> listCustomers(int page, int size) {
        return customerRepository.findAll(PageRequest.of(page, size))
                .map(customerMapper::mapToCustomerDTO);
    }

    public CustomerDetailsDTO getCustomerDetails(String customerId) {
        Customer customer = findCustomerById(customerId);
        return CustomerDetailsDTO.builder()
                .id(customer.getId())
                .email(customer.getEmail())
                .firstname(customer.getFirstname())
                .lastname(customer.getLastname())
                .active(customer.isActive())
                .paymentDetails(null) // TODO
                .addressDetails(null) // TODO
                .blacklistDetails(CustomerDetailsDTO.BlacklistDetails.builder()
                        .blacklisted(false) // TODO
                        .build())
                .build();
    }

    private Customer findCustomerById(String id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
    }

    public CustomerStatsResponse getCustomerStats() {
        var stats = new CustomerStatsResponse();

        stats.setTotalCustomers(customerRepository.count());

        return stats;
    }

}