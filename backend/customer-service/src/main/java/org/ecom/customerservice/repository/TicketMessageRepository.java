package org.ecom.customerservice.repository;

import java.util.List;
import java.util.UUID;

import org.ecom.customerservice.model.TicketMessage;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketMessageRepository extends JpaRepository<TicketMessage, UUID> {

    List<TicketMessage> findByTicketId(Long ticketId);

    List<TicketMessage> findByTicketId(Long ticketId, Sort sort);

}
