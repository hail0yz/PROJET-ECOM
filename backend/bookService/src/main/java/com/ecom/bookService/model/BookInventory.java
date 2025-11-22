package com.ecom.bookService.model;

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
@Table(name="book_inventories")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class BookInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    private Book book;

    // ----------- Inventory / Entrepôt -----------

    private int availableQuantity;

    private int reservedQuantity;

    private int minimumStockLevel;

    // ----------- Optimistic Locking -----------

    @Version
    private Long version;

    // ----------- Audit -----------

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public boolean canReserve(int quantity) {
        return availableQuantity - reservedQuantity >= quantity;
    }

    public void reserve(int quantity) {
        if (!canReserve(quantity)) {
            throw new IllegalStateException("Not enough stock");
        }
        reservedQuantity += quantity;
    }

    public void confirmReservation(int quantity) {
        if (reservedQuantity < quantity) {
            throw new IllegalStateException("Reservation mismatch");
        }
        reservedQuantity -= quantity;
        availableQuantity -= quantity;
    }

    public void cancelReservation(int quantity) {
        if (reservedQuantity < quantity) {
            throw new IllegalStateException("Invalid cancellation");
        }
        reservedQuantity -= quantity;
    }

}
