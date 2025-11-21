package org.ecom.customerservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecom.customerservice.dto.CustomerDTO;
import org.ecom.customerservice.dto.CustomerDetailsDTO;
import org.ecom.customerservice.dto.CustomerPreferencesDTO;
import org.ecom.customerservice.dto.CustomerProfileDTO;
import org.ecom.customerservice.dto.UpdatePreferencesRequest;
import org.ecom.customerservice.exception.EntityNotFoundException;
import org.ecom.customerservice.mapper.CustomerMapper;
import org.ecom.customerservice.model.Customer;
import org.ecom.customerservice.model.Preferences;
import org.ecom.customerservice.repository.CustomerRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    private final CustomerMapper customerMapper;

    public CustomerDTO getCustomerById(String customerId) {
        Customer customer = findCustomerById(customerId);
        return customerMapper.mapToCustomerDTO(customer);
    }

    public CustomerProfileDTO getCustomerProfile(String customerId) {
        Customer customer = findCustomerById(customerId);
        return customerMapper.mapToCustomerProfileDTO(customer);
    }

    public void updateCustomerPreferences(String customerId, UpdatePreferencesRequest request) {
        Preferences preferences = Preferences.builder()
                .emailNotificationsEnabled(request.emailNotificationsEnabled())
                .smsNotificationsEnabled(request.smsNotificationsEnabled())
                .build();

        Customer customer = findCustomerById(customerId);
        customer.setPreferences(preferences);
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
                .lastname(customer.getFirstname())
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

}