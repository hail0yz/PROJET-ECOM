package org.ecom.customerservice.mapper;

import java.util.Optional;

import org.springframework.stereotype.Component;

import org.ecom.customerservice.dto.CustomerDTO;
import org.ecom.customerservice.dto.CustomerPreferencesDTO;
import org.ecom.customerservice.dto.CustomerProfileDTO;
import org.ecom.customerservice.model.Contact;
import org.ecom.customerservice.model.Customer;
import org.ecom.customerservice.model.PhoneNumber;

@Component
public class CustomerMapper {

    public CustomerDTO mapToCustomerDTO(Customer customer) {
        if (customer == null) {
            return null;
        }

        return CustomerDTO.builder()
                .id(customer.getId())
                .externalId(customer.getExternalId())
                .firstname(customer.getFirstname())
                .lastname(customer.getLastname())
                .email(customer.getEmail())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    public CustomerProfileDTO mapToCustomerProfileDTO(Customer customer) {
        if (customer == null) {
            return null;
        }

        String phoneNumber = Optional.ofNullable(customer.getContact())
                .map(Contact::getPhoneNumber)
                .map(PhoneNumber::getNumber)
                .orElse(null);

        return CustomerProfileDTO.builder()
                .id(customer.getId())
                .firstname(customer.getFirstname())
                .lastname(customer.getLastname())
                .name(customer.getName())
                .avatar(customer.getAvatar())
                .email(customer.getEmail())
                .phoneNumber(phoneNumber)
                .build();
    }

    public CustomerPreferencesDTO mapToCustomerPreferencesDTO(Customer customer) {
        if (customer == null || customer.getPreferences() == null) {
            return null;
        }

        return CustomerPreferencesDTO.builder()
                .id(customer.getId())
                .emailNotificationsEnabled(customer.getPreferences().isEmailNotificationsEnabled())
                .smsNotificationsEnabled(customer.getPreferences().isSmsNotificationsEnabled())
                .build();
    }

}
