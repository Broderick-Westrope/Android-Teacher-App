package com.broderickwestrope.whiteboard.exams.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.broderickwestrope.whiteboard.R;
import com.broderickwestrope.whiteboard.exams.ExamEditor;
import com.broderickwestrope.whiteboard.exams.Models.ExamModel;
import com.broderickwestrope.whiteboard.exams.Utils.ExamDBManager;
import com.broderickwestrope.whiteboard.exams.ViewExamActivity;
import com.broderickwestrope.whiteboard.exams.ViewRecordActivity;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// The wrapper/adapter between the database and the recycler view
public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.ViewHolder> {

    private final Activity activity; // The activity that is using this adapter to display the exams
    private final ExamDBManager db;  // Our database manager for the exams (using SQLite)
    private List<ExamModel> examList; // A list of all of our exams

    private int card_futureColor = R.color.yellow_green;
    private int card_runningColor = R.color.turquoise_blue;
    private int card_pastColor = R.color.texas_rose;

    // Class constructor
    public ExamAdapter(ExamDBManager db, Activity activity) {
        this.activity = activity; // Set the containing activity
        this.db = db; // Set the database being used
    }

    // Inflates (ie. sets up) the given exam/card view
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exam, parent, false);
        return new ViewHolder(itemView);
    }

    // Sets up the contents of a view holder with the data of a specified exam (from the database)
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onBindViewHolder(ViewHolder holder, int index) {
        db.openDatabase(); // Open the exam database for use
        ExamModel item = examList.get(index); // Get the exam from the list using the specified index
        String timeTillExam = timeTillExam(item.getDate(), item.getTime(), item.getDuration()); // Get the time till the exam

        holder.name.setText(item.getName()); // Set the name to the exam's name
        holder.unit.setText(item.getUnit()); // Set the unit to the exam's unit
        holder.timeBetween.setText(timeTillExam); // Set the date to the exam's date

        // We want to give each 'card' (each exam) a specific background based on the status of the exam
        if (timeTillExam.startsWith("Starts")) // If the exam is yet to start we use green
            holder.card.setBackgroundColor(ContextCompat.getColor(activity, card_futureColor));
        else if (timeTillExam.startsWith("Ends")) // If the exam is in progress we use blue
            holder.card.setBackgroundColor(ContextCompat.getColor(activity, card_runningColor));
        else // If the exam is over we use yellow/orange
            holder.card.setBackgroundColor(ContextCompat.getColor(activity, card_pastColor));

        // Listen for clicks on the "SEE MORE" button to view the students exams
        holder.card.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                // Takes us to a new activity listing the information for this exam
                viewExam(item);
            }
        });
    }

    // Takes us to a new activity listing the information for this exam
    @RequiresApi(api = Build.VERSION_CODES.N)
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
        b.putString("time between", timeTillExam(exam.getDate(), exam.getTime(), exam.getDuration()));

        //Put the bundle of extras in the intent and start the activity
        i.putExtras(b);
        activity.startActivity(i);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String timeTillExam(String _examDate, String _examTime, float duration) {
        // Combine the exam date and time values in the correct format
        _examTime = _examDate + " " + _examTime;

        // Get the current date and time
        Date c = Calendar.getInstance().getTime();

        // Format the current date and time to the correct format (to match the exam date and time)
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String currentTime = df.format(c);
        long tillStart = 0, tillEnd = 0;

        try {
            // Turn the dates from strings to the Date type
            Date currentDate = df.parse(currentTime);
            Date examStart = df.parse(_examTime);

            // Get the different between the two times, giving us the time till the exam starts. Negative means that the exam has begun. These times are stored as the amount of milliseconds.
            tillStart = examStart.getTime() - currentDate.getTime();

            int durationMillis = (int) duration * (60 * 60 * 1000);
            Calendar cal = Calendar.getInstance();
            cal.setTime(examStart);
            cal.add(Calendar.MILLISECOND, durationMillis);
            tillEnd = cal.getTime().getTime() - currentDate.getTime();
        } catch (Exception exception) {
            Toast.makeText(activity, "ERROR: Unable to find difference in dates.", Toast.LENGTH_SHORT).show();
        }

        String timeLeft;
        // If the difference is positive then we have time left before the exam begins
        if (tillStart > 0) {
            if (tillStart / (1000 * 60 * 60 * 24 * 7) > 0) {
                tillStart /= (1000 * 60 * 60 * 24 * 7);
                timeLeft = "Starts in " + Long.toString(tillStart) + " Week";
            } else if (tillStart / (1000 * 60 * 60 * 24) > 0) {
                tillStart /= (1000 * 60 * 60 * 24);
                timeLeft = "Starts in " + Long.toString(tillStart) + " Day";
            } else if (tillStart / (1000 * 60 * 60) > 0) {
                tillStart /= (1000 * 60 * 60);
                timeLeft = "Starts in " + Long.toString(tillStart) + " Hour";
            } else if (tillStart / (1000 * 60) > 0) {
                tillStart /= (1000 * 60);
                timeLeft = "Starts in " + Long.toString(tillStart) + " Minute";
            } else if (tillStart / 1000 > 0) {
                tillStart /= (1000);
                timeLeft = "Starts in " + Long.toString(tillStart) + " Second";
            } else {
                timeLeft = "Starts in " + Long.toString(tillStart) + " Millisecond";
            }

            if (tillStart > 1)
                timeLeft += "s";

        } else {
            timeLeft = (tillEnd > 0) ? "Ends in " : "Ended ";
            String suffix = (tillEnd > 0) ? "" : " Ago";
            tillEnd = Math.abs(tillEnd);

            if (tillEnd / (1000 * 60 * 60 * 24 * 7) > 0) {
                tillEnd /= (1000 * 60 * 60 * 24 * 7);
                timeLeft += Long.toString(tillEnd) + " Week";
            } else if (tillEnd / (1000 * 60 * 60 * 24) > 0) {
                tillEnd /= (1000 * 60 * 60 * 24);
                timeLeft += Long.toString(tillEnd) + " Day";
            } else if (tillEnd / (1000 * 60 * 60) > 0) {
                tillEnd /= (1000 * 60 * 60);
                timeLeft += Long.toString(tillEnd) + " Hour";
            } else if (tillEnd / (1000 * 60) > 0) {
                tillEnd /= (1000 * 60);
                timeLeft += Long.toString(tillEnd) + " Minute";
            } else if (tillEnd / 1000 > 0) {
                tillEnd /= (1000);
                timeLeft += Long.toString(tillEnd) + " Second";
            } else {
                timeLeft += Long.toString(tillEnd) + " Millisecond";
            }

            if (tillEnd > 1)
                timeLeft += "s";
            timeLeft += suffix;
        }
        return timeLeft;
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
        Snackbar.make(getContext(), activity.findViewById(R.id.content), "Exam Deleted.", Snackbar.LENGTH_LONG).setAction("UNDO", new View.OnClickListener() {
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
        fragment.show(((ViewRecordActivity) activity).getSupportFragmentManager(), ExamEditor.TAG); // Display the fragment
    }

    // Returns the current activity
    public Context getContext() {
        return activity;
    }

    // Create a version of the RecyclerView ViewHolder with added views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, unit, timeBetween; // TextViews for remaining components of the student exam
        RelativeLayout card; // A "card" that everything is displayed on (this is so we can change the color)

        ViewHolder(View view) {
            super(view); // Execute the base function
            name = view.findViewById(R.id.exam_Name); // Set the text view to the one in exam_layout
            unit = view.findViewById(R.id.exam_Unit); // Set the text view to the one in exam_layout
            timeBetween = view.findViewById(R.id.exam_TimeBetween); // Set the text view to the one in exam_layout
            card = view.findViewById(R.id.layoutCard); // Set the relative layout to the one in exam_layout
        }
    }
}
