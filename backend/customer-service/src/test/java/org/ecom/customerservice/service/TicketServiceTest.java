package org.ecom.customerservice.service;

import java.util.List;
import java.util.Optional;

import jakarta.validation.ConstraintViolationException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.ecom.customerservice.dto.CreateTicketRequest;
import org.ecom.customerservice.dto.CreateTicketResponse;
import org.ecom.customerservice.dto.TicketCategoryDTO;
import org.ecom.customerservice.dto.TicketDTO;
import org.ecom.customerservice.exception.EntityNotFoundException;
import org.ecom.customerservice.mapper.TicketMapper;
import org.ecom.customerservice.model.Customer;
import org.ecom.customerservice.model.Ticket;
import org.ecom.customerservice.model.TicketCategory;
import org.ecom.customerservice.repository.CustomerRepository;
import org.ecom.customerservice.repository.TicketCategoryRepository;
import org.ecom.customerservice.repository.TicketRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    private final String CUSTOMER_ID = "123";
    private final Long TICKET_ID = 99L;
    private final int PAGE = 0;
    private final int SIZE = 10;

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private TicketMapper ticketMapper;
    @Mock
    private TicketCategoryRepository ticketCategoryRepository;
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private TicketService ticketService;

    @Captor
    ArgumentCaptor<Ticket> ticketCaptor;

    private Customer customer;

    private Ticket ticket;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(CUSTOMER_ID);

        ticket = new Ticket();
        ticket.setId(TICKET_ID);
        ticket.setCustomer(customer);
    }

    // --------- TicketService.getTicketById ---------

    // TODO implement tests for getTicketById

    // --------- TicketService.getCustomerTickets ---------

    @Test
    void getCustomerTickets_ShouldReturnPagedDTOs_WhenSortIsValid() {
        String sortParam = "date";
        String sortProperty = "createdAt";
        PageRequest page = PageRequest.of(PAGE, SIZE, Sort.Direction.DESC, sortProperty);

        Ticket ticketA = new Ticket();
        Ticket ticketB = new Ticket();
        Page<Ticket> ticketPage = new PageImpl<>(List.of(ticketA, ticketB));

        when(ticketRepository.findByCustomerId(CUSTOMER_ID, page)).thenReturn(ticketPage);
        when(ticketMapper.mapToTicketDTO(any(Ticket.class))).thenReturn(new TicketDTO());

        Page<TicketDTO> resultPage = ticketService.getCustomerTickets(CUSTOMER_ID, PAGE, SIZE, sortParam);

        assertNotNull(resultPage);
        assertEquals(2, resultPage.getContent().size());
        verify(ticketRepository).findByCustomerId(CUSTOMER_ID, page);
        verify(ticketMapper, times(2)).mapToTicketDTO(any(Ticket.class));
    }

    @Test
    void getCustomerTickets_shouldThrowConstraintViolation_whenSortIsInvalid() {
        String invalidSort = "priority";

        assertThrows(ConstraintViolationException.class,
                () -> ticketService.getCustomerTickets(CUSTOMER_ID, PAGE, SIZE, invalidSort));

        verify(ticketRepository, never()).findByCustomerId(any(), any());
    }

    // --------- TicketService.createTicket ---------

    @Test
    void createTicket_shouldCreateTicketAndReturnId_whenCustomerExists() {
        CreateTicketRequest request = CreateTicketRequest.builder()
                .subject("Subject")
                .description("Description...")
                .type(Ticket.Type.OTHER)
                .build();

        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));

        when(ticketRepository.save(ticketCaptor.capture())).thenAnswer(invocation -> {
            Ticket capturedTicket = invocation.getArgument(0);
            capturedTicket.setId(TICKET_ID);
            return capturedTicket;
        });

        CreateTicketResponse response = ticketService.createTicket(CUSTOMER_ID, request);

        assertNotNull(response);
        assertEquals(TICKET_ID, response.getTicketId());

        verify(ticketRepository).save(any(Ticket.class));

        Ticket capturedTicket = ticketCaptor.getValue();
        assertEquals(request.subject(), capturedTicket.getSubject());
        assertEquals(request.description(), capturedTicket.getDescription());
    }

    @Test
    void createTicket_shouldThrowException_whenCustomerDoesNotExist() {
        CreateTicketRequest request = CreateTicketRequest.builder()
                .subject("Subject")
                .description("Description...")
                .type(Ticket.Type.OTHER)
                .build();
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> ticketService.createTicket(CUSTOMER_ID, request));

        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    // --------- TicketService.listTicketCategories ---------

    @Test
    void listTicketCategories_shouldReturnPagedCategoryDTOs() {
        Pageable pageRequest = PageRequest.of(PAGE, SIZE);

        TicketCategory categoryA = new TicketCategory();
        TicketCategory categoryB = new TicketCategory();
        Page<TicketCategory> categoryPage = new PageImpl<>(List.of(categoryA, categoryB));

        when(ticketCategoryRepository.findAll(pageRequest)).thenReturn(categoryPage);
        when(ticketMapper.mapTicketCategoryToDTO(any(TicketCategory.class))).thenReturn(new TicketCategoryDTO());

        Page<TicketCategoryDTO> resultPage = ticketService.listTicketCategories(PAGE, SIZE);

        assertNotNull(resultPage);
        assertEquals(2, resultPage.getContent().size());
        verify(ticketCategoryRepository).findAll(pageRequest);
        verify(ticketMapper, times(2)).mapTicketCategoryToDTO(any(TicketCategory.class));
    }

}
