package com.example.lab5.provider;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "books")
public class Book {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "bookId")
    private int bookId;

    @ColumnInfo(name = "bookTitle")
    private String title;

    @ColumnInfo(name = "bookISBN")
    private String ISBN;

    @ColumnInfo(name = "bookAuthor")
    private String author;

    @ColumnInfo(name = "bookDescription")
    private String description;

    @ColumnInfo(name = "bookPrice")
    private String price;

    public Book(String title, String ISBN, String author, String description, String price) {
        this.title = title;
        this.ISBN = ISBN;
        this.author = author;
        this.description = description;
        this.price = price;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(@NonNull int bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
