package org.ecom.customerservice.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.ecom.customerservice.dto.MonthlyCount;
import org.ecom.customerservice.dto.PriorityCount;
import org.ecom.customerservice.dto.TypeCount;
import org.ecom.customerservice.dto.WeeklyCount;
import org.ecom.customerservice.model.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByIdAndCustomerId(Long ticketId, String customerId);

    Page<Ticket> findByCustomerId(String customerId, Pageable pageable);

    List<Ticket> findByAssignedTo(String assignedTo);

    @Query("SELECT t.priority as priority, COUNT(t) as count " +
            "FROM Ticket t " +
            "GROUP BY t.priority")
    List<PriorityCount> countByPriority();

    @Query("SELECT t.type as type, COUNT(t) as count " +
            "FROM Ticket t " +
            "GROUP BY t.type")
    List<TypeCount> countByType();

    @Query("SELECT EXTRACT(YEAR FROM t.createdAt) as year, " +
            "EXTRACT(WEEK FROM t.createdAt) as week, " +
            "COUNT(t) as count " +
            "FROM Ticket t " +
            "WHERE t.createdAt >= :startDate " +
            "GROUP BY EXTRACT(YEAR FROM t.createdAt), EXTRACT(WEEK FROM t.createdAt) " +
            "ORDER BY year, week")
    List<WeeklyCount> countByWeek(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT TO_CHAR(t.createdAt, 'YYYY-MM') as month, " +
            "COUNT(t) as count " +
            "FROM Ticket t " +
            "WHERE t.createdAt >= :startDate " +
            "GROUP BY TO_CHAR(t.createdAt, 'YYYY-MM') " +
            "ORDER BY month")
    List<MonthlyCount> countByMonth(@Param("startDate") LocalDateTime startDate);

}
