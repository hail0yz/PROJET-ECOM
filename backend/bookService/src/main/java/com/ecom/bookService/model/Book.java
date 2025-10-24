package com.ecom.bookService.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name="Books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="CATEGORY_FK")
    @JsonIgnoreProperties("books")
    private Category category;

    private String title;
    private String author;
    private Double price;
    private int stock;
    private String summary;


    public Long getId() { return bookId; }
    public void setId(Long bookId) { this.bookId = bookId; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

}
