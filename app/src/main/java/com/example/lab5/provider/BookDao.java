package com.example.lab5.provider;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BookDao {

    @Query("select * from books")
    LiveData<List<Book>> getAllBooks();

    @Query("select * from books where bookTitle=:title")
    List<Book> getBook(String title);

    @Insert
    void addBook(Book book);

    @Query("delete from books where bookTitle= :title")
    void deleteBook(String title);

    @Query("delete FROM books")
    void deleteAllBooks();

    @Query("delete from books where bookId = (SELECT Max(bookId) FROM books)")
    void deleteLastBook();
}
