package org.ecom.customerservice.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecom.customerservice.dto.CustomerDTO;
import org.ecom.customerservice.dto.CustomerPreferencesDTO;
import org.ecom.customerservice.dto.CustomerProfileDTO;
import org.ecom.customerservice.dto.UpdatePreferencesRequest;

@Service
@Slf4j
//@RequiredArgsConstructor
public class CustomerService {

    public CustomerDTO getCustomerById(String contactId) {
        return null;
    }

    public CustomerProfileDTO getCustomerProfile(String customerId) {
        return null; // TODO
    }

    public void updatePreferences(String customerId, UpdatePreferencesRequest request) {
        // TODO
    }

    public CustomerPreferencesDTO getCustomerPreferences(String customerId) {
        return null;
    }

    public void updateCustomerPreferences(String customerId) {
        // TODO
    }

    public Page<CustomerDTO> listCustomers(int page, int size) {
        return null; // TODO
    }

}