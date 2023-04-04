package com.example.lab5;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.StringTokenizer;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private List<String> myList;

    public MyRecyclerViewAdapter(List<String> data) {
        myList = data;
    }

    @NonNull
    @Override
    public MyRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyRecyclerViewAdapter.ViewHolder holder, int position) {

        StringTokenizer myTokenizer = new StringTokenizer(myList.get(position), "|");
        holder.bookIDView.setText(myTokenizer.nextToken());
        holder.titleView.setText(myTokenizer.nextToken());
        holder.ISBNView.setText(myTokenizer.nextToken());
        holder.authorView.setText(myTokenizer.nextToken());
        holder.descriptionView.setText(myTokenizer.nextToken());
        holder.priceView.setText(myTokenizer.nextToken());

        final int fPosition = position;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Item at position " + fPosition + "was clicked!", Snackbar.LENGTH_LONG);
            }
        });
    }

    @Override
    public int getItemCount() {
        return myList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView bookIDView;
        public TextView titleView;
        public TextView authorView;
        public TextView ISBNView;
        public TextView descriptionView;
        public TextView priceView;

        public ViewHolder(View itemView) {
            super(itemView);

            bookIDView = itemView.findViewById(R.id.bookIDCardText);
            titleView = itemView.findViewById(R.id.titleCardText);
            authorView = itemView.findViewById(R.id.authorCardText);
            ISBNView = itemView.findViewById(R.id.ISBNCardText);
            descriptionView = itemView.findViewById(R.id.descriptionCardText);
            priceView = itemView.findViewById(R.id.priceCardText);
        }
    }
}
