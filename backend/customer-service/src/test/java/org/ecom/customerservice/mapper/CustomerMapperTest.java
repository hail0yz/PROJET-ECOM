package org.ecom.customerservice.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.ecom.customerservice.dto.CustomerDTO;
import org.ecom.customerservice.dto.CustomerPreferencesDTO;
import org.ecom.customerservice.dto.CustomerProfileDTO;
import org.ecom.customerservice.model.Customer;
import org.ecom.customerservice.model.Preferences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomerMapperTest {

    private CustomerMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CustomerMapper();
    }

    @Test
    void mapToCustomerDTO_nullInput_returnsNull() {
        CustomerDTO dto = mapper.mapToCustomerDTO(null);
        assertNull(dto);
    }

    @Test
    void mapToCustomerDTO_populatedFields_mapsCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        Customer customer = Customer.builder()
                .id("c-1")
                .externalId("ext-1")
                .firstname("First")
                .lastname("Last")
                .email("me@example.com")
                .createdAt(now)
                .updatedAt(now)
                .build();

        CustomerDTO dto = mapper.mapToCustomerDTO(customer);

        assertEquals("c-1", dto.getId());
        assertEquals("ext-1", dto.getExternalId());
        assertEquals("First", dto.getFirstname());
        assertEquals("Last", dto.getLastname());
        assertEquals("me@example.com", dto.getEmail());
    }

    @Test
    void mapToCustomerProfileDTO_nullInput_returnsNull() {
        CustomerProfileDTO dto = mapper.mapToCustomerProfileDTO(null);
        assertNull(dto);
    }

    @Test
    void mapToCustomerProfileDTO_populated_mapsCorrectly() {
        Customer customer = Customer.builder()
                .id("p-1")
                .firstname("Jane")
                .lastname("Doe")
                .name("Jane Doe")
                .avatar("/img.png")
                .email("jane@example.com")
                .build();

        CustomerProfileDTO dto = mapper.mapToCustomerProfileDTO(customer);

        assertEquals("p-1", dto.getId());
        assertEquals("Jane", dto.getFirstname());
        assertEquals("Doe", dto.getLastname());
        assertEquals("Jane Doe", dto.getName());
        assertEquals("/img.png", dto.getAvatar());
        assertEquals("jane@example.com", dto.getEmail());
    }

    @Test
    void mapToCustomerPreferencesDTO_nullInput_returnsNull() {
        CustomerPreferencesDTO dto = mapper.mapToCustomerPreferencesDTO(null);
        assertNull(dto);
    }

    @Test
    void mapToCustomerPreferencesDTO_populated_mapsCorrectly() {
        Preferences prefs = Preferences.builder()
                .emailNotificationsEnabled(true)
                .smsNotificationsEnabled(false)
                .build();

        Customer customer = Customer.builder()
                .id("pref-1")
                .preferences(prefs)
                .build();

        CustomerPreferencesDTO dto = mapper.mapToCustomerPreferencesDTO(customer);

        assertEquals("pref-1", dto.getId());
        assertEquals(true, dto.isEmailNotificationsEnabled());
        assertEquals(false, dto.isSmsNotificationsEnabled());
    }
}
