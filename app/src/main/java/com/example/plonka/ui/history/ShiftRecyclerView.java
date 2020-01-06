package com.example.plonka.ui.history;

import android.content.Context;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.plonka.R;
import com.example.plonka.Shift;

import java.util.ArrayList;

// Heavily based on example code at: https://developer.android.com/guide/topics/ui/layout/recyclerview

public class ShiftRecyclerView extends RecyclerView.Adapter<ShiftRecyclerView.MyViewHolder> {
    private ArrayList<Shift> mDataset = new ArrayList<>();
    private Context mContext;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textViewTimestamp;
        public TextView textViewStatus;

        public MyViewHolder(View v) {
            super(v);
            textViewTimestamp = v.findViewById(R.id.textViewTimestamp);
            textViewStatus = v.findViewById(R.id.textViewStatus);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ShiftRecyclerView(ArrayList<Shift> myDataset, Context c) {
        mDataset = myDataset;
        mContext = c;
    }


    // Create new views (invoked by the layout manager)
    @Override
    public ShiftRecyclerView.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shift_recycler_view_item, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        holder.textViewTimestamp.setText(mDataset.get(position).getTimestamp());
        String status = mDataset.get(position).getStatus();
        holder.textViewStatus.setText(status);
        if (status.equals("OK")){
            holder.textViewStatus.setTextColor(Color.GREEN);
        }
        else if (status.equals("Denied")){
            holder.textViewStatus.setTextColor(Color.RED);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
