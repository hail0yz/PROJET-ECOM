package org.ecom.customerservice.model;

import java.time.LocalDateTime;
import java.util.List;

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

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_type")
    private Type type;

    private Priority priority;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TicketMessage> messages;

    private String assignedTo;

    // -------- Ticket Context --------

    private String productId;

    private String orderId;

    // -------- Metrics --------

    private LocalDateTime firstResponseAt;

    private LocalDateTime resolvedAt;

    private LocalDateTime closedAt;

    @Version
    private Long version;

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
        REOPENED,
        ESCALATED
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
