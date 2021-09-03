package com.broderickwestrope.whiteboard.exams.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.broderickwestrope.whiteboard.R;
import com.broderickwestrope.whiteboard.exams.ExamEditor;
import com.broderickwestrope.whiteboard.exams.Models.ExamModel;
import com.broderickwestrope.whiteboard.exams.Utils.ExamDBManager;
import com.broderickwestrope.whiteboard.exams.ViewExamActivity;
import com.broderickwestrope.whiteboard.exams.ViewRecordActivity;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Random;

// The wrapper/adapter between the database and the recycler view
public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.ViewHolder> {

    private final ViewRecordActivity activity; // The activity that is using this adapter to display the exams
    private final ExamDBManager db;  // Our database manager for the exams (using SQLite)
    private List<ExamModel> examList; // A list of all of our exams

    // Class constructor
    public ExamAdapter(ExamDBManager db, ViewRecordActivity activity) {
        this.activity = activity; // Set the containing activity
        this.db = db; // Set the database being used
    }

    // Inflates (ie. sets up) the given exam/card view
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_exam, parent, false);
        return new ViewHolder(itemView);
    }

    // Sets up the contents of a view holder with the data of a specified exam (from the database)
    public void onBindViewHolder(ViewHolder holder, int index) {
        db.openDatabase(); // Open the exam database for use
        ExamModel item = examList.get(index); // Get the exam from the list using the specified index

        holder.name.setText(item.getName()); // Set the name to the exam's name
        holder.unit.setText(item.getUnit()); // Set the unit to the exam's unit
        holder.date.setText(item.getDate()); // Set the date to the exam's date
        holder.time.setText(item.getTime()); // Set the time to the exam's time

        // Select a random color for card background (from the given array) :)
        Random random = new Random();
        String[] colorArray = getContext().getResources().getStringArray(R.array.card_colors);
        String randomColorName = colorArray[random.nextInt(colorArray.length)];
        holder.card.setBackgroundColor(Color.parseColor(randomColorName));

        // Listen for clicks on the "SEE MORE" button to view the students exams
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Takes us to a new activity listing the information for this exam
                viewExam(item);
            }
        });
    }

    // Takes us to a new activity listing the information for this exam
    public void viewExam(ExamModel exam) {
        // Create a new intend and bundle to go to the new activity and pass the data to it
        Intent i = new Intent(activity, ViewExamActivity.class);
        Bundle b = new Bundle();

        // Put all the information in the bundle for the next activity
        b.putString("name", exam.getName()); // Put in the exam name
        b.putString("unit", exam.getUnit()); // Put in the exam unit
        b.putString("date", exam.getDate()); // Put in the exam date
        b.putString("time", exam.getTime()); // Put in the exam time
        b.putString("location", exam.getLocation()); // Put in the exam location
        b.putFloat("duration", exam.getDuration()); // Put in the exam duration

        //Put the bundle of extras in the intent and start the activity
        i.putExtras(b);
        activity.startActivity(i);
    }

    // Returns the number of exams in our list of exams (ie. the length of the to-do list)
    public int getItemCount() {
        return examList.size();
    }

    // Set the local list of exams to the given list of exams
    public void setExams(List<ExamModel> todoList) {
        this.examList = todoList; // Set local to the given exams
        notifyDataSetChanged(); // Notify any concerned members that the data has changed (this is mainly for updating the recycler view)
    }

    // Used to delete a exam at the given index
    public void deleteItem(int index) {
        ExamModel item = examList.get(index); // Get the item from the list
        db.deleteExam(item.getExamID()); // Remove the item from the database
        examList.remove(index); // Remove the item from the list
        notifyItemRemoved(index); // Update the recycler view

        // Create a snackbar to say that the entry was deleted and allow the user to undo this if it was a mistake
        Snackbar.make(getContext(), activity.findViewById(R.id.content), "Exam Deleted.", Snackbar.LENGTH_SHORT).setAction("UNDO", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                examList.add(item); // Add to the local list
                db.insertExam(item); // Add to the database
                notifyItemInserted(examList.size() - 1); // Update the recycler view
            }
        }).show();
    }

    // Used to delete all exam items
    public void deleteAll() {
        int size = examList.size();
        for (int i = 0; i < size; i++) {
            ExamModel item = examList.get(i); // Get the item from the list
            db.deleteExam(item.getExamID()); // Remove the item from the database
        }

        examList.clear(); // Remove the item from the list
        notifyItemRangeRemoved(0, size); // Tell the recycler view that elements were removed at the given position
    }

    // Edit the item at the given index
    public void editExam(int index) {
        ExamModel exam = examList.get(index); // Get the exam at the given index
        Bundle bundle = new Bundle(); // Create a new bundle to hold our data (this is how we detect if we are editing or creating in the ExamEditor)
        bundle.putInt("id", exam.getExamID());
        bundle.putString("name", exam.getName());
        bundle.putString("unit", exam.getUnit());
        bundle.putString("date", exam.getDate());
        bundle.putString("time", exam.getTime());
        bundle.putString("location", exam.getLocation());
        bundle.putFloat("duration", exam.getDuration());

        ExamEditor fragment = new ExamEditor(activity, exam.getStudentId()); // Create a new ExamEditor fragment
        fragment.setArguments(bundle); // Put the bundle in the fragment
        fragment.show(activity.getSupportFragmentManager(), ExamEditor.TAG); // Display the fragment
    }

    // Returns the current activity
    public Context getContext() {
        return activity;
    }

    // Create a version of the RecyclerView ViewHolder with added views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, unit, date, time; // TextViews for remaining components of the student exam
        RelativeLayout card; // A "card" that everything is displayed on (this is so we can change the color)

        ViewHolder(View view) {
            super(view); // Execute the base function
            name = view.findViewById(R.id.exam_Name); // Set the text view to the one in exam_layout
            unit = view.findViewById(R.id.exam_Unit); // Set the text view to the one in exam_layout
            date = view.findViewById(R.id.exam_Date); // Set the text view to the one in exam_layout
            time = view.findViewById(R.id.exam_Time); // Set the text view to the one in exam_layout
            card = view.findViewById(R.id.layoutCard); // Set the relative layout to the one in exam_layout
        }
    }
}
