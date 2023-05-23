package com.example.lab5.provider;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class BookViewModel extends AndroidViewModel {

    private BookRepository mRepository;
    private LiveData<List<Book>> mAllBooks;

    public BookViewModel(@NonNull Application application) {
        super(application);
        mRepository = new BookRepository(application);
        mAllBooks = mRepository.getAllBooks();
    }

    public LiveData<List<Book>> getAllBooks() {
        return mAllBooks;
    }

    public void insert(Book book) {
        mRepository.insert(book);
    }
    public void deleteAll(){
        mRepository.deleteAll();
    }

    public void deleteLastBook(){
        mRepository.deleteLastBook();
    }

    public void deleteUnknown() {
        mRepository.deleteUnknown();
    }

    public void deleteBook(String title) {
        mRepository.deleteBook(title);
    }
}
