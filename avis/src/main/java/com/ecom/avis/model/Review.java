package com.ecom.avis.model;

public class Review {
    private int id;
    private String description;
    private int note;
    private int userId;
    private int bookId;

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", note=" + note +
                ", userId=" + userId +
                ", bookId=" + bookId +
                '}';
    }

    public Review(int id, String description, int note, int userId, int bookId) {
        this.id = id;
        this.description = description;
        this.note = note;
        this.userId = userId;
        this.bookId = bookId;
    }


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }
    public Review(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }

}

