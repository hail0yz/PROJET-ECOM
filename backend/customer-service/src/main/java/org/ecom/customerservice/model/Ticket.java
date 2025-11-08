package org.ecom.customerservice.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Customer customer;

    private String subject;

    private String description;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_type")
    private Type type;

    @ManyToOne
    private TicketCategory category;

    // -------- Ticket Context --------

    private String productId;

    private String orderId;

    // -------- Metrics --------

    private LocalDateTime firstResponseAt;

    private LocalDateTime resolvedAt;

    private LocalDateTime closedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum Status {
        OPEN,
        IN_PROGRESS,
        WAITING_CUSTOMER,
        WAITING_VENDOR,
        RESOLVED,
        CLOSED,
        REOPENED
    }

    public enum Type {
        ORDER_ISSUE,
        PAYMENT_ISSUE,
        PRODUCT_QUERY,
        ACCOUNT_ISSUE,
        FEEDBACK,
        OTHER
    }

}
