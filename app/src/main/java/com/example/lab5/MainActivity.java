package com.example.lab5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lab5.provider.Book;
import com.example.lab5.provider.BookViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // INSTANCE VARS                                                                             //
    //////////////////////////////////////////////////////////////////////////////////////////////

    // Firebase related
    FirebaseDatabase firebaseDatabase;
    DatabaseReference myRef;

    // DB related
    private BookViewModel mBookViewModel;

    // Recycle view related
    RecyclerView myRecyclerView;
    RecyclerView.LayoutManager myLayoutManager;
    MyRecyclerViewAdapter myAdapter;

    // edittext related
    EditText bookIDText;
    EditText titleText;
    EditText ISBNText;
    EditText authorText;
    EditText descriptionText;
    EditText priceText;

    // drawer / navigation view related
    DrawerLayout drawerLayout;

    // gesture/motion related
    View myFrame;
    int xdown;
    int ydown;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ANDROID LIFECYCLE STATES                                                                  //
    //////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer);

          //////////////////////////////////////////////////////////////////////////////////////////
         // INTENT FILTER                                                                        //
        //////////////////////////////////////////////////////////////////////////////////////////

        /* Request permissions to access SMS */
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, 0);

        // create intent filter
        IntentFilter intentFilter = new IntentFilter("MySMS");
        // register the broadcast receiver
        registerReceiver(myReceiver, intentFilter);

         ///////////////////////////////////////////////////////////////////////////////////////////
        // UI ELEMENTS                                                                           //
       ///////////////////////////////////////////////////////////////////////////////////////////

        // initialize and bind all edit text objects
        bookIDText = findViewById(R.id.editTextBookID);
        titleText = findViewById(R.id.editTextTitle);
        ISBNText = findViewById(R.id.editTextISBN);
        authorText = findViewById(R.id.editTextAuthor);
        descriptionText = findViewById(R.id.editTextDescription);
        priceText = findViewById(R.id.editTextPrice);

        // initialize and bind the toolbar then set is as the action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // initialize and bind the fab button
        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton);
        // set the onclick event to add book
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addBook();
            }
        });

        // initialize and bind the drawer layout
        drawerLayout = findViewById(R.id.drawerLayout);
        // create the action bar toggle icon
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // initialize and bind navigation view
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(new MyNavigationListener()); // set the listener to listener object

        // put the recycle view fragment into the frame layout
        getSupportFragmentManager().beginTransaction().replace(R.id.frame1, new RecycleViewFragment()).commit();

        // gesture detector
        GestureDetector gestureDetector;

        //////////////////////////////////////////////////////////////////////////////////////////
        // DATABASE                                                                             //
        //////////////////////////////////////////////////////////////////////////////////////////
        myAdapter = new MyRecyclerViewAdapter();
        mBookViewModel = new ViewModelProvider(this).get(BookViewModel.class);
        mBookViewModel.getAllBooks().observe(this, newData -> {
            myAdapter.setMyList(newData);
            myAdapter.notifyDataSetChanged();
        });

        // fire base related
        firebaseDatabase = FirebaseDatabase.getInstance();
        myRef = firebaseDatabase.getReference("Books");

        //////////////////////////////////////////////////////////////////////////////////////////
        // GESTURE                                                                             //
        ////////////////////////////////////////////////////////////////////////////////////////
        myFrame = findViewById(R.id.touchZone);
        gestureDetector = new GestureDetector(this, new MyGestureDetector());

        myFrame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadBook();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // special callbacks here onwards
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState); // saves all view data to the bundle
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState); // restore all view data
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // get the id of selected item
        int id = item.getItemId();
        if (id == R.id.clearFields) {
            clearFields();
        } else if (id == R.id.loadData) {
            loadBook();
        } else if (id == R.id.totalBooks) {
            totalBooks();
        }
        return super.onOptionsItemSelected(item);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // METHODS                                                                                   //
    //////////////////////////////////////////////////////////////////////////////////////////////

    public void onButtonShowPopupWindowClick(View view) {

        // inflate the layout of the popup window
        View popupView = getLayoutInflater().inflate(R.layout.popup_window, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        EditText popUpInput = popupView.findViewById(R.id.popUpInput);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                String title = popUpInput.getText().toString();
                mBookViewModel.deleteBook(title);
                myAdapter.notifyDataSetChanged();
                popupWindow.dismiss();
                return true;
            }
        });
    }

    public void addBook() {
        // declare book attributes
        String bookId;
        String title;
        int ISBN;
        String author;
        String description;
        double price;

        // start by collecting all relevant book data from plain text fields
        bookId = bookIDText.getText().toString(); // set book id's value to the input text using get text, also type casts to string
        title = titleText.getText().toString();
        ISBN = (ISBNText.getText().toString().equals("")) ? -1 : (Integer.parseInt(ISBNText.getText().toString())); // retrieve as string first then parse into int or double as needed, fancy if else ternary operator
        author = authorText.getText().toString();
        description = descriptionText.getText().toString();
        price = (priceText.getText().toString().equals("")) ? 0 : (Double.parseDouble(priceText.getText().toString())); // more ternary operator shenanigans

        // now that we have collected all our input data we can create our toast message and display it
        // creating the toast object and message
        Toast myMessage = Toast.makeText(this, String.format("Book (%s) and the price (%.2f)", title, price), Toast.LENGTH_LONG);
        myMessage.show(); // actually displaying the message

        // now we can save all of the data into a shared pref object to persist it
        SharedPreferences myBook = getSharedPreferences("book", 0); // create shared pref
        SharedPreferences.Editor myEditor = myBook.edit(); // create editor obj

        // start saving all the data
        myEditor.putString("id", bookId);
        myEditor.putString("title", title);
        myEditor.putString("isbn", Integer.toString(ISBN));
        myEditor.putString("author", author);
        myEditor.putString("description", description);
        myEditor.putString("price", Double.toString(price));

        myEditor.apply(); // apply the changes

        Book book = new Book(title, Integer.toString(ISBN), author, description, Double.toString(price));

        // add the book to the listview
        mBookViewModel.insert(book);
        myAdapter.notifyDataSetChanged();

        // add the book to firebase
        myRef.push().setValue(book);
    }

    public void clearFields() { // simple method calling clear on the edit text objects text attribute
        bookIDText.getText().clear();
        titleText.getText().clear();
        ISBNText.getText().clear();
        authorText.getText().clear();
        descriptionText.getText().clear();
        priceText.getText().clear();
    }

    private void loadBook() {
        // create shared pref
        SharedPreferences myBook = getSharedPreferences("book", 0);

        // set all the edit text back to the saved values
        bookIDText.setText(myBook.getString("id", ""));
        titleText.setText(myBook.getString("title", ""));
        ISBNText.setText(myBook.getString("isbn", ""));
        authorText.setText(myBook.getString("author", ""));
        descriptionText.setText(myBook.getString("description", ""));
        priceText.setText(myBook.getString("price", ""));
    }

    private void removePrevious() {
        mBookViewModel.deleteLastBook();
        myAdapter.notifyDataSetChanged();
    }

    private void removeAll() {
        mBookViewModel.deleteAll();
        myAdapter.notifyDataSetChanged();

        // delete all firebase books
        myRef.removeValue();
    }

    private void totalBooks() {
        Toast myMessage = Toast.makeText(this, String.format("Total books is %s", mBookViewModel.getAllBooks().getValue().size()), Toast.LENGTH_LONG);
        myMessage.show(); // actually displaying the message
    }

    private void listAll() {
        startActivity(new Intent(getApplicationContext(), MainActivity2.class));
    }

    private void deleteUnknown() {
        mBookViewModel.deleteUnknown();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // BROADCAST RECEIVER                                                                        //
    //////////////////////////////////////////////////////////////////////////////////////////////

    BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // now we get the book data string
            String bookData = intent.getStringExtra("bookData");
            Toast.makeText(context, bookData, Toast.LENGTH_LONG).show();

            // create string tokenizer with | as the delimiter
            //"12|Harry Potter|32451|J. K. Rowling|Fantasy|45" format
            StringTokenizer myTokenizer = new StringTokenizer(bookData, "|");

            // set the text of everything
            bookIDText.setText(myTokenizer.nextToken());
            titleText.setText(myTokenizer.nextToken());
            ISBNText.setText(myTokenizer.nextToken());
            authorText.setText(myTokenizer.nextToken());
            descriptionText.setText(myTokenizer.nextToken());

            Double price = Double.parseDouble(myTokenizer.nextToken());
            Boolean bool = Boolean.parseBoolean(myTokenizer.nextToken());

            if (bool) {
                price += 100;
            } else {
                price += 5;
            }

            priceText.setText(Double.toString(price));
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // NAVIGATION VIEW LISTENER                                                                  //
    //////////////////////////////////////////////////////////////////////////////////////////////

    class MyNavigationListener implements NavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            // get the id of selected item
            int id = item.getItemId();
            if (id == R.id.addBook) {
                addBook();
            } else if (id == R.id.removeLast) {
                removePrevious();
            } else if (id == R.id.removeAll) {
                removeAll();
            } else if (id == R.id.close) {
                finish();
            } else if (id == R.id.listAll) {
                listAll();
            } else if (id == R.id.deleteUnknown) {
                deleteUnknown();
            } else if (id == R.id.deleteBook) {
                onButtonShowPopupWindowClick(findViewById(R.id.drawerLayout));
            }
            drawerLayout.closeDrawers();
            return true;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // GESTURE DETECTOR                                                                          //
    //////////////////////////////////////////////////////////////////////////////////////////////

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent e) {
            ISBNText.setText(RandomString.generateNewRandomString(5));
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {

            if (e1.getX() > e2.getX()) {
                Double price = Double.parseDouble(priceText.getText().toString());
                priceText.setText(Double.toString(price + distanceX));
            } else {
                Double price = Double.parseDouble(priceText.getText().toString());
                priceText.setText(Double.toString(price - distanceX));
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            if (velocityX > 1000) {
                moveTaskToBack(true);
            }

            if (velocityY > 1000) {
                titleText.setText("untitled");
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {
            clearFields();
            return super.onDoubleTap(e);
        }

        @Override
        public void onLongPress(@NonNull MotionEvent e) {
            loadBook();
            super.onLongPress(e);
        }


    }
}