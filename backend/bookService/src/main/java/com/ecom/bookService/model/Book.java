package com.ecom.bookService.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name="Books")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="CATEGORY_FK")
    @JsonIgnoreProperties("books")
    private Category category;

    private String title;

    private String subtitle;

    private String author;

    private BigDecimal price;

    private int stock;

    @Column(length = 10_000)
    private String summary;

    private String isbn10; // https://isbn-international.org

    private String isbn13;

    private String thumbnail;

    private Integer publishedYear;

    private Integer numPages;

    @OneToOne(mappedBy = "book", cascade = CascadeType.ALL, optional = false, orphanRemoval = true)
    private BookInventory inventory;

    // ----------- Optimistic Locking -----------

    @Version
    private Long version;

    // ----------- Audit -----------

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void setInventory(BookInventory inventory) {
        this.inventory = inventory;
        inventory.setBook(this);
    }

}
