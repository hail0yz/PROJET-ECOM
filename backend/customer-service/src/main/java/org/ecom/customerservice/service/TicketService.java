package org.ecom.customerservice.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.validation.ConstraintViolationException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

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
import org.ecom.customerservice.exception.EntityNotFoundException;
import org.ecom.customerservice.mapper.TicketMapper;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import org.ecom.customerservice.model.Customer;
import org.ecom.customerservice.model.Priority;
import org.ecom.customerservice.model.Ticket;
import org.ecom.customerservice.model.TicketMessage;
import org.ecom.customerservice.repository.CustomerRepository;
import org.ecom.customerservice.repository.TicketCategoryRepository;
import org.ecom.customerservice.repository.TicketMessageRepository;
import org.ecom.customerservice.repository.TicketRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class TicketService {

    private static final Map<String, String> SORT_FIELD_MAP = Map.of(
            "date", "createdAt",
            "id", "id"
    );
    private static final String ERROR_MESSAGE_SORT = "Invalid sort parameter: %s. Valid values are: %s";

    private final TicketRepository ticketRepository;

    private final TicketMessageRepository ticketMessageRepository;

    private final TicketMapper ticketMapper;

    private final CustomerRepository customerRepository;

    private final TicketCategoryRepository ticketCategoryRepository;

    public TicketDTO getCustomerTicket(String customerId, Long ticketId) {
        log.info("Getting ticket {} for customer {}", ticketId, customerId);
        Ticket ticket = ticketRepository.findByIdAndCustomerId(ticketId, customerId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found : " + ticketId));
        return ticketMapper.mapToTicketDTOWithMessages(ticket);
    }

    public TicketDTO getTicketById(Long ticketId) {
        log.info("Getting ticket {}", ticketId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found : " + ticketId));
        return ticketMapper.mapToTicketDTOWithMessages(ticket);
    }

    public Page<TicketDTO> getCustomerTickets(String customerId, int page, int size, String sort) {
        log.info("Getting tickets for customer {} with page {}, size {}, sort {}", customerId, page, size, sort);
        String property = SORT_FIELD_MAP.get(sort);
        if (property == null) {
            log.error("Invalid sort parameter: {}", sort);
            throw createConstraintViolation(sort);
        }

        Pageable pageRequest = PageRequest.of(page, size, Sort.Direction.DESC, property);

        return ticketRepository.findByCustomerId(customerId, pageRequest)
                .map(ticketMapper::mapToTicketDTO);
    }

    @Transactional
    public CreateTicketResponse createTicket(String customerId, CreateTicketRequest request) {
        log.info("Creating ticket for customer {}", customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found : " + customerId));

        Priority priority = request.priority() == null
                ? Priority.MEDIUM
                : request.priority();

        Ticket ticket = Ticket.builder()
                .customer(customer)
                .subject(request.subject())
                .description(request.description())
                .status(Ticket.Status.OPEN)
                .priority(priority)
                .type(request.type())
                .build();

        Ticket saved = ticketRepository.save(ticket);

        log.info("Created ticket {} for customer {}", saved.getId(), customerId);
        return new CreateTicketResponse(saved.getId());
    }

    @Transactional(readOnly = true)
    public TicketStatsResponse getTicketStats() {
        log.info("Getting ticket stats");
        TicketStatsResponse stats = new TicketStatsResponse();

        stats.setTotalTickets(ticketRepository.count());

        stats.setCountsByPriority(getCountsByPriority());
        stats.setCountsByType(getCountsByType());
        stats.setWeeklyCounts(getWeeklyCounts());
        stats.setMonthlyCounts(getMonthlyCounts());

        return stats;
    }

    @Transactional(readOnly = true)
    public Page<TicketCategoryDTO> listTicketCategories(int page, int size) {
        return ticketCategoryRepository.findAll(PageRequest.of(page, size))
                .map(ticketMapper::mapTicketCategoryToDTO);
    }

    public Page<TicketDTO> listCustomerTickets(String customerId, int page, int size) {
        log.info("Listing tickets for customer {}", customerId);
        return ticketRepository.findByCustomerId(customerId, PageRequest.of(page, size))
                .map(ticketMapper::mapToTicketDTO);
    }

    public Page<TicketDTO> listTickets(int page, int size) {
        log.info("Listing all tickets");
        return ticketRepository.findAll(PageRequest.of(page, size))
                .map(ticketMapper::mapToTicketDTO);
    }

    @Transactional
    public void assignTo(Long ticketId, String userId, Collection<String> roles) {
        log.info("Assigning ticket {} to support user {}", ticketId, userId);

        if (!isSupportOrAdmin(roles)) {
            log.error("User is not allowed to assign tickets");
            throw new AccessDeniedException("User is not allowed to assign tickets");
        }

        // todo fetch the user from keycloak

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));

        ticket.setAssignedTo(userId);
        ticketRepository.save(ticket);
    }

    @Transactional
    public CreateTicketMessageResponse addMessage(Long ticketId, CreateTicketMessageRequest request, String authorId, Collection<String> roles) {
        log.info("Adding message to ticket {} by author {}", ticketId, authorId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));

        if (isCustomer(roles)) {
            if (!ticket.getCustomer().getId().equals(authorId)) {
                throw new AccessDeniedException("Customer does not own the ticket");
            }
        }
        else if (!isSupportOrAdmin(roles)) {
            throw new AccessDeniedException("User is not allowed to add messages to tickets");
        }

        if (ticket.getStatus() == Ticket.Status.CLOSED || Ticket.Status.RESOLVED == ticket.getStatus()) {
            throw new IllegalStateException("Cannot add message to a closed/resolved ticket");
        }

        TicketMessage message = TicketMessage.builder()
                .authorId(authorId)
                .content(request.content())
                .authorRole(pickRole(roles))
                .ticket(ticket)
                .build();

        TicketMessage saved = ticketMessageRepository.save(message);
        return CreateTicketMessageResponse.builder()
                .ticketId(message.getId())
                .messageId(saved.getId())
                .build();
    }

    private boolean isSupportOrAdmin(Collection<String> roles) {
        return roles.contains("ROLE_SUPPORT") || roles.contains("ROLE_ADMIN");
    }

    private boolean isCustomer(Collection<String> roles) {
        return roles.contains("ROLE_USER");
    }

    private String pickRole(Collection<String> roles) {
        return roles.stream()
                .filter(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_SUPPORT") || role.equals("ROLE_USER"))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("User does not have a valid role"));
    }

    private boolean hasAccess(Collection<String> roles, String userId, Long ticketId) {
        log.info("Checking access for user {} to ticket {}", userId, ticketId);
        if (isCustomer(roles)) {
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));
            log.info("Ticket {} belongs to customer {}", ticketId, ticket.getCustomer().getId());
            return Objects.equals(ticket.getCustomer().getId(), userId);
        }
        return isSupportOrAdmin(roles);
    }

    /**
     * Helper usable from SpEL (@PreAuthorize) to check if the current authentication
     * has access to a given ticket. Controller can call this via @ticketService.canAccessTicket(...)
     */
    public boolean canAccessTicket(Authentication authentication, String userId, Long ticketId) {
        if (authentication == null) return false;

        Collection<String> roles = AuthorizationUtils.getAuthorities(authentication);
        log.info("User {} with roles {} is requesting access to ticket {}", userId, roles, ticketId);
        return hasAccess(roles, userId, ticketId);
    }

    private ConstraintViolationException createConstraintViolation(String invalidSort) {
        return new ConstraintViolationException(
                String.format(ERROR_MESSAGE_SORT, invalidSort, SORT_FIELD_MAP.keySet()),
                null
        );
    }

    private Map<String, Long> getCountsByType() {
        Map<String, Long> map = new LinkedHashMap<>();

        for (Ticket.Type t : Ticket.Type.values()) {
            map.put(t.name(), 0L);
        }
        map.put("UNKNOWN", 0L);

        ticketRepository.countByType()
                .forEach(tc -> map.put(
                        tc.getType() != null ? tc.getType() : Ticket.Type.OTHER.name(),
                        tc.getCount()
                ));

        return map;
    }

    private Map<String, Long> getWeeklyCounts() {
        LocalDate now = LocalDate.now();
        LocalDateTime startDate = now.minusWeeks(12).atStartOfDay();
        WeekFields wf = WeekFields.ISO;

        Map<String, Long> weeklyCounts = new LinkedHashMap<>();
        for (int i = 11; i >= 0; i--) {
            LocalDate d = now.minusWeeks(i);
            String key = String.format("%04d-W%02d",
                    d.get(wf.weekBasedYear()),
                    d.get(wf.weekOfWeekBasedYear()));
            weeklyCounts.put(key, 0L);
        }

        ticketRepository.countByWeek(startDate)
                .forEach(wc -> {
                    String key = String.format("%04d-W%02d", wc.getYear(), wc.getWeek());
                    weeklyCounts.put(key, wc.getCount());
                });

        return weeklyCounts;
    }

    private Map<String, Long> getMonthlyCounts() {
        LocalDate now = LocalDate.now();
        LocalDateTime startDate = now.minusMonths(12)
                .withDayOfMonth(1)
                .atStartOfDay();
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("yyyy-MM");

        Map<String, Long> monthlyCounts = new LinkedHashMap<>();
        for (int i = 11; i >= 0; i--) {
            LocalDate d = now.minusMonths(i).withDayOfMonth(1);
            monthlyCounts.put(d.format(monthFmt), 0L);
        }

        ticketRepository.countByMonth(startDate)
                .forEach(mc -> monthlyCounts.put(mc.getMonth(), mc.getCount()));

        return monthlyCounts;
    }

    private Map<String, Long> getCountsByPriority() {
        Map<String, Long> map = new LinkedHashMap<>();

        for (Priority p : Priority.values()) {
            map.put(p.name(), 0L);
        }

        ticketRepository.countByPriority()
                .forEach(pc -> map.put(
                        pc.getPriority() != null ? pc.getPriority() : "UNKNOWN",
                        pc.getCount()
                ));

        return map;
    }

    @Transactional
    public TicketDTO closeTicket(Long ticketId, String userId) {
        log.info("Closing ticket {} by user {}", ticketId, userId);
        
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found: " + ticketId));

        if (ticket.getStatus() == Ticket.Status.CLOSED) {
            log.warn("Ticket {} is already closed", ticketId);
            throw new IllegalStateException("Le ticket est déjà fermé");
        }

        ticket.setStatus(Ticket.Status.CLOSED);
        Ticket saved = ticketRepository.save(ticket);
        
        log.info("Ticket {} closed successfully", ticketId);
        return ticketMapper.mapToTicketDTOWithMessages(saved);
    }

    @Transactional
    public TicketDTO changeTicketStatus(Long ticketId, Ticket.Status newStatus, String userId) {
        log.info("Changing ticket {} status to {} by user {}", ticketId, newStatus, userId);
        
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found: " + ticketId));

        ticket.setStatus(newStatus);
        Ticket saved = ticketRepository.save(ticket);

        log.info("Ticket {} status changed successfully to {}", ticketId, newStatus);
        return ticketMapper.mapToTicketDTO(saved);
    }

}
