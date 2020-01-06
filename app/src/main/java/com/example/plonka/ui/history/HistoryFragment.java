package com.example.plonka.ui.history;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plonka.R;
import com.example.plonka.Shift;

import java.util.ArrayList;

public class HistoryFragment extends Fragment {

    private String LOG_TAG = "PLONKA_HISTORY_FRAGMENT";

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private Integer userId;
    private String userPw;

    private ArrayList<Shift> shiftList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_history, container, false);

        userId = getActivity().getIntent().getExtras().getInt("userId");
        userPw = getActivity().getIntent().getExtras().getString("userPw");

        Log.d(LOG_TAG, "Using user: "+userId+", pw: "+userPw+" when fetching sessions...");

        // Handle recycler view containing list of recordings
        recyclerView = (RecyclerView) root.findViewById(R.id.shiftRecyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new ShiftRecyclerView(shiftList, getActivity().getApplicationContext());
        recyclerView.setAdapter(mAdapter);

        // Get shift information for current user from DB:
        AsyncGetShiftsTask shiftsTask = new AsyncGetShiftsTask(new AsyncGetShiftsTask.AsyncResponse(){
            @Override
            public void processFinish(ArrayList<Shift> output){
                Log.d(LOG_TAG, "processFinish() called");
            }
        });
        try{
            shiftList = shiftsTask.execute(userId.toString(), userPw).get();
        } catch (Exception e) {
            Log.e(LOG_TAG, "AsyncGetShiftsTask.execute() failed: "+e.toString());
        }

        Log.i(LOG_TAG, "Got the following " + shiftList.size() + " shifts from DB:");
        for (Shift s : shiftList){
            Log.i(LOG_TAG, " >> "+s.getTimestamp()+" - " + s.getStatus());
        }
        Log.i(LOG_TAG, "End of shifts from DB.");
        updateShiftList();

        return root;
    }

    /**
     * Updates the UI by updating the recycler with sessionsList (from DB). No protection for incorrect formatting provided.
     */
    private void updateShiftList() {
        Log.d(LOG_TAG, "Called updateShiftList()");
        if (shiftList.size() > 0) {
            Log.d(LOG_TAG, "Updating ShiftRecyclerView");
            mAdapter = new ShiftRecyclerView(shiftList, getActivity().getApplicationContext());
            recyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        }
    }


}
