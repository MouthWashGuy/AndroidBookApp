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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
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

//        myFrame.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ;
//            }
//        });

        myFrame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int action = event.getActionMasked();

                switch(action) {
                    case MotionEvent.ACTION_DOWN:
                        xdown = (int) event.getX();
                        ydown = (int) event.getY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        return true;
                    case MotionEvent.ACTION_UP:

                        // tolerence check for x coord gestures
                        if (Math.abs(ydown - event.getY()) < 40) {
                            if (xdown - event.getX() < 0) {
                                Double currPrice = priceText.getText().toString().equals("") ? 0.0 : Double.parseDouble(priceText.getText().toString());
                                currPrice += 1;
                                priceText.setText(Double.toString(currPrice));
                            } else if  (xdown - event.getX() > 0){
                                addBook();
                            }
                        } else if (Math.abs(xdown - event.getX()) < 40) {
                            if (ydown - event.getY() > 0) {
                                clearFields();
                            } else if (ydown - event.getY() < 0) {
                                finish();
                            }
                        }

                        if (xdown < 50 && ydown < 50) {
                            String input = authorText.getText().toString();
                            String output = input.toUpperCase();
                            authorText.setText(output);
                        }

                        return true;
                    default:
                        return false;
                }
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
            }
            drawerLayout.closeDrawers();
            return true;
        }
    }
}