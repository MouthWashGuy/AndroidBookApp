package com.example.lab5.provider;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class BookRepository {

    private BookDao mBookDao;
    private LiveData<List<Book>> mAllBooks;

    BookRepository(Application application) {
        BookDatabase db = BookDatabase.getDatabase(application);
        mBookDao = db.bookDao();
        mAllBooks = mBookDao.getAllBooks();
    }
    LiveData<List<Book>> getAllBooks() {
        return mAllBooks;
    }
    void insert(Book book) {
        BookDatabase.databaseWriteExecutor.execute(() -> mBookDao.addBook(book));
    }

    void deleteAll(){
        BookDatabase.databaseWriteExecutor.execute(()->{
            mBookDao.deleteAllBooks();
        });
    }

    void deleteLastBook(){
        BookDatabase.databaseWriteExecutor.execute(()->{
            mBookDao.deleteLastBook();
        });
    }

    void deleteUnknown(){
        BookDatabase.databaseWriteExecutor.execute(()->{
            mBookDao.deleteUnknown();
        });
    }

    void deleteBook(String title){
        BookDatabase.databaseWriteExecutor.execute(()->{
            mBookDao.deleteBook(title);
        });
    }
}
