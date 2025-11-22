package org.ecom.customerservice.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.ecom.customerservice.model.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByIdAndCustomerId(Long ticketId, String customerId);

    Page<Ticket> findByCustomerId(String customerId, Pageable pageable);

}
