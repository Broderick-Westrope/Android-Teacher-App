package com.broderickwestrope.whiteboard;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.broderickwestrope.whiteboard.R;
import com.broderickwestrope.whiteboard.exams.Adapters.ExamAdapter;
import com.broderickwestrope.whiteboard.exams.Models.ExamModel;
import com.broderickwestrope.whiteboard.exams.Utils.ExamDBManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private ExamAdapter examsAdapter; // The wrapper/adapter between the database and the recycler view
    private List<ExamModel> examList; // A list of all of our exams
    private ExamDBManager db; // Our database manager for the exams (using SQLite)

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Create our database manager and open it for use
        db = new ExamDBManager(getActivity());
        db.openDatabase();

        // Get the recycler view that contains our list of exams
        RecyclerView examsRV = requireView().findViewById(R.id.examsRV);
        examsRV.setLayoutManager(new LinearLayoutManager(getActivity()));
        examsAdapter = new ExamAdapter(db, getActivity()); // Create a new adapter
        examsRV.setAdapter(examsAdapter); // Attach the adapter to the recycler view

        examList = new ArrayList<>(); // Create our local list of exams
        examList = db.getUpcomingExams(getDateAndTime()); // Assign any existing exams from the database
        examsAdapter.setExams(examList); // Set the recycler view to contain these exams (using the adapter)
    }

    public Pair<String, String> getDateAndTime() {
        String today, weeksTime;
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        today = day + "/" + month + "/" + year;

        calendar.add(Calendar.DAY_OF_MONTH, 7);
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        weeksTime = day + "/" + month + "/" + year;
        return new Pair<>(today, weeksTime);
    }
}
