package org.ecom.customerservice.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import org.ecom.customerservice.dto.CustomerDTO;
import org.ecom.customerservice.dto.CustomerPreferencesDTO;
import org.ecom.customerservice.dto.CustomerProfileDTO;
import org.ecom.customerservice.dto.UpdatePreferencesRequest;
import org.ecom.customerservice.exception.EntityNotFoundException;
import org.ecom.customerservice.mapper.CustomerMapper;
import org.ecom.customerservice.model.Customer;
import org.ecom.customerservice.repository.CustomerRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceUnitTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private CustomerDTO customerDTO;
    private CustomerProfileDTO customerProfileDTO;
    private CustomerPreferencesDTO customerPreferencesDTO;

    private final String CUSTOMER_ID = "123";

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(CUSTOMER_ID);

        customerDTO = new CustomerDTO();
        customerProfileDTO = new CustomerProfileDTO();
        customerPreferencesDTO = new CustomerPreferencesDTO();
    }

    // -------------------- CustomerService.getCustomerById --------------------

    @Test
    void getCustomerById_ShouldReturnCustomerDTO_WhenCustomerExists() {
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(customerMapper.mapToCustomerDTO(customer)).thenReturn(customerDTO);

        CustomerDTO result = customerService.getCustomerById(CUSTOMER_ID);

        assertThat(result).isEqualTo(customerDTO);
        verify(customerRepository).findById(CUSTOMER_ID);
        verify(customerMapper).mapToCustomerDTO(customer);
    }

    @Test
    void getCustomerById_ShouldThrowException_WhenCustomerDoesNotExist() {
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                customerService.getCustomerById(CUSTOMER_ID)
        );

        verify(customerMapper, never()).mapToCustomerDTO(any());
    }

    // -------------------- CustomerService.getCustomerProfile --------------------

    @Test
    void getCustomerProfile_ShouldReturnProfileDTO_WhenCustomerExists() {
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(customerMapper.mapToCustomerProfileDTO(customer)).thenReturn(customerProfileDTO);

        CustomerProfileDTO result = customerService.getCustomerProfile(CUSTOMER_ID);

        assertThat(result).isEqualTo(customerProfileDTO);
        verify(customerRepository).findById(CUSTOMER_ID);
        verify(customerMapper).mapToCustomerProfileDTO(customer);
    }

    @Test
    void updateCustomerPreferences_shouldReturnPreferencesDTO() {
        var request = new UpdatePreferencesRequest(true, false);

        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));

        customerService.updateCustomerPreferences(CUSTOMER_ID, request);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());

        Customer savedCustomer = captor.getValue();
        assertNotNull(savedCustomer.getPreferences());
        assertThat(savedCustomer.getPreferences().isEmailNotificationsEnabled()).isTrue();
        assertThat(savedCustomer.getPreferences().isSmsNotificationsEnabled()).isFalse();
    }

    // -------------------- CustomerService.getCustomerById --------------------

    @Test
    void getCustomerPreferences_ShouldReturnPreferencesDTO() {
        var expected = new CustomerPreferencesDTO(CUSTOMER_ID, false, true);

        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(customerMapper.mapToCustomerPreferencesDTO(customer)).thenReturn(expected);

        CustomerPreferencesDTO result = customerService.getCustomerPreferences(CUSTOMER_ID);

        assertNotNull(result);
        assertEquals(CUSTOMER_ID, result.getId());
        assertFalse(result.isEmailNotificationsEnabled());
        assertTrue(result.isSmsNotificationsEnabled());

        verify(customerRepository).findById(CUSTOMER_ID);
        verify(customerMapper).mapToCustomerPreferencesDTO(customer);
    }

    @Test
    void getCustomerPreferences_ShouldThrowException_WhenCustomerDoesNotExist() {
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                customerService.getCustomerPreferences(CUSTOMER_ID)
        );
        verify(customerMapper, never()).mapToCustomerPreferencesDTO(any());
    }

    // -------------------- CustomerService.listCustomers --------------------

    @Test
    void listCustomers_shouldReturnEmptyPage_whenNoCustomersExist() {
        int page = 0;
        int size = 10;
        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Customer> emptyPage = Page.empty(pageRequest);

        when(customerRepository.findAll(pageRequest)).thenReturn(emptyPage);

        Page<CustomerDTO> result = customerService.listCustomers(page, size);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(customerRepository).findAll(pageRequest);
        verify(customerMapper, never()).mapToCustomerDTO(any());
    }

    @Test
    void listCustomers_shouldReturnPagedCustomerDTOs() {
        int page = 0;
        int size = 2;
        Pageable pageable = PageRequest.of(page, size);

        Customer c1 = buildCustomer("2", "Bob");
        Page<Customer> customerPage = new PageImpl<>(List.of(customer, c1), pageable, 10);

        when(customerRepository.findAll(pageable)).thenReturn(customerPage);

        when(customerMapper.mapToCustomerDTO(customer)).thenReturn(buildCustomerDTO(CUSTOMER_ID, "Name 1"));
        when(customerMapper.mapToCustomerDTO(c1)).thenReturn(buildCustomerDTO("2", "Name 2"));

        Page<CustomerDTO> resultPage = customerService.listCustomers(page, size);

        assertNotNull(resultPage);
        assertEquals(2, resultPage.getContent().size());
        assertEquals(10, resultPage.getTotalElements());
        assertEquals("Name 1", resultPage.getContent().get(0).getName());
        assertEquals("Name 2", resultPage.getContent().get(1).getName());

        verify(customerRepository, times(1)).findAll(pageable);
    }

    private Customer buildCustomer(String id, String name) {
        return Customer.builder()
                .id(id)
                .name(name)
                .build();
    }

    private CustomerDTO buildCustomerDTO(String id, String name) {
        return CustomerDTO.builder()
                .id(id)
                .name(name)
                .build();
    }

}
