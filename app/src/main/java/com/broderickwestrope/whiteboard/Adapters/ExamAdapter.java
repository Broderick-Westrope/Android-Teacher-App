package com.broderickwestrope.whiteboard.Adapters;

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

import com.broderickwestrope.whiteboard.Editors.ExamEditor;
import com.broderickwestrope.whiteboard.Models.ExamModel;
import com.broderickwestrope.whiteboard.R;
import com.broderickwestrope.whiteboard.Utils.ExamDBManager;
import com.broderickwestrope.whiteboard.exams.ViewExamActivity;
import com.broderickwestrope.whiteboard.exams.ViewRecordActivity;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// The wrapper/adapter between the database and the recycler view
public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.ViewHolder> {

    private final Activity activity; // The activity that is using this adapter to display the exams
    private final ExamDBManager db;  // Our database manager for the exams (using SQLite)
    // Colors for the exam 'cards'. Different colors represent different status'
    int card_futureColor = R.color.yellow_green; // This is the color for when the exam is in the future
    int card_runningColor = R.color.turquoise_blue; // This is the color for when the exam is currently running
    int card_pastColor = R.color.texas_rose; // This is the color for when the exam is in the past
    private List<ExamModel> examList; // A list of all of the exams in the recycler view
    private List<ExamModel> selectedList; // A list of all selected (ie. highlighted) exams


    // Class constructor
    public ExamAdapter(ExamDBManager db, Activity activity) {
        this.activity = activity; // Set the containing activity
        this.db = db; // Set the database being used
    }

    // Inflates (ie. sets up) the given exam
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

        //Initialise the list of selected exams
        selectedList = new ArrayList<>();

        holder.name.setText(item.getName()); // Set the name to the exam's name
        holder.unit.setText(item.getUnit()); // Set the unit to the exam's unit
        holder.timeBetween.setText(timeTillExam); // Set the date to the exam's date

        // We want to give each 'card' (each exam) a specific background based on the status of the exam
        if (timeTillExam.startsWith("Starts")) // If the exam is yet to start
            holder.card.setBackgroundColor(ContextCompat.getColor(activity, card_futureColor));
        else if (timeTillExam.startsWith("Ends")) // If the exam is in progress
            holder.card.setBackgroundColor(ContextCompat.getColor(activity, card_runningColor));
        else // If the exam is over
            holder.card.setBackgroundColor(ContextCompat.getColor(activity, card_pastColor));

        // Listen for clicks on the card (used to view the students exams)
        holder.card.setOnClickListener(v -> {
            // Takes us to a new activity listing the information for this exam
            viewExams(item);
        });

        // Listen for long clicks on the card (used to toggle if the exam is selected)
        holder.card.setOnLongClickListener(v -> {
            // Toggle if the exam is or isn't selected and make the update visual
            toggleSelected(item, holder.card, timeTillExam);
            return false;
        });
    }

    // Go to a new activity listing the information for this exam
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void viewExams(ExamModel exam) {
        // Create a new intent and bundle to go to the new activity and pass data to the activity
        Intent i = new Intent(activity, ViewExamActivity.class);
        Bundle b = new Bundle();

        // Put all the information in the bundle for the next activity
        b.putString("name", exam.getName()); // Put in the name
        b.putString("unit", exam.getUnit()); // Put in the unit
        b.putString("date", exam.getDate()); // Put in the date
        b.putString("time", exam.getTime()); // Put in the time
        b.putString("location", exam.getLocation()); // Put in the location
        b.putFloat("duration", exam.getDuration()); // Put in the duration
        // Put in the string for the time between now and the exam
        b.putString("time between", timeTillExam(exam.getDate(), exam.getTime(), exam.getDuration()));

        //Put the bundle of extras in the intent and start the activity
        i.putExtras(b);
        activity.startActivity(i);
    }

    // Toggle whether or not the exam is selected (for deletion)
    private void toggleSelected(ExamModel exam, RelativeLayout card, String timeTillExam) {
        // If the exam is successfully removed
        if (selectedList.remove(exam)) {
            // We want to give each 'card' (each exam) a specific background based on the status of the exam (these colors were defined above)
            if (timeTillExam.startsWith("Starts")) // If the exam is yet to start
                card.setBackgroundColor(ContextCompat.getColor(activity, card_futureColor));
            else if (timeTillExam.startsWith("Ends")) // If the exam is in progress
                card.setBackgroundColor(ContextCompat.getColor(activity, card_runningColor));
            else // If the exam is over
                card.setBackgroundColor(ContextCompat.getColor(activity, card_pastColor));
        } else { //Else, if it wasn't removed (ie. it wasn't in the list of selected exams)
            selectedList.add(exam); // Add the exam to the list of selected exams
            // Set the color to a light-grey to visually represent the selection
            card.setBackgroundColor(activity.getResources().getColor(R.color.geyser));
        }
    }

    // Get a string containing a message of when the exam was/is
    @RequiresApi(api = Build.VERSION_CODES.N)
    public String timeTillExam(String _examDate, String _examTime, float duration) {
        // Combine the exam date and time values in the correct format
        _examTime = _examDate + " " + _examTime;

        // Get the current date and time
        Date c = Calendar.getInstance().getTime();

        // Format the current date and time to the correct format (to match the exam date and time)
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()); // Define how we want to format it
        String currentTime = df.format(c); // Format it

        // Initialise the variables we use for defining hop long until the start and end of the exam.
        // These are stored as longs because they will hold (potentially very large) millisecond values.
        long tillStart = 0, tillEnd = 0;

        try {
            // Turn the date-times from strings to the Date type
            Date currentDate = df.parse(currentTime); // The date-time of the present
            Date examStart = df.parse(_examTime); // The date-time of the exam

            // Get the different between the two date-times, giving us the time till the exam starts.
            // Negative means that the exam has begun. These times are stored as the amount of milliseconds.
            tillStart = examStart.getTime() - currentDate.getTime();

            //Convert the exam duration from hours (how the user inputs it) to milliseconds (this is the unit we deal with)
            int durationMillis = (int) duration * (60 * 60 * 1000);

            // Create a calendar instance with the date-time of the exam's finish
            Calendar cal = Calendar.getInstance(); // Get an instance of the calendar
            cal.setTime(examStart); // Set it to the date-time of the exam
            cal.add(Calendar.MILLISECOND, durationMillis); // Add on the duration of the exam
            // Get the different between the two date-times, giving us the time till the exam ends.
            tillEnd = cal.getTime().getTime() - currentDate.getTime();
        } catch (Exception exception) {
            // Provide feedback for the failed operations
            Toast.makeText(activity, "ERROR: Unable to find difference in dates.", Toast.LENGTH_LONG).show();
        }

        // Create the string which stores our message
        String timeBetween;
        if (tillStart > 0) {// If the difference is positive then we have time left before the exam begins
            // Measure how long till it starts to use the appropriate unit of measure
            if (tillStart / (1000 * 60 * 60 * 24 * 7) > 0) {
                tillStart /= (1000 * 60 * 60 * 24 * 7);
                timeBetween = "Starts in " + Long.toString(tillStart) + " Week";
            } else if (tillStart / (1000 * 60 * 60 * 24) > 0) {
                tillStart /= (1000 * 60 * 60 * 24);
                timeBetween = "Starts in " + Long.toString(tillStart) + " Day";
            } else if (tillStart / (1000 * 60 * 60) > 0) {
                tillStart /= (1000 * 60 * 60);
                timeBetween = "Starts in " + Long.toString(tillStart) + " Hour";
            } else if (tillStart / (1000 * 60) > 0) {
                tillStart /= (1000 * 60);
                timeBetween = "Starts in " + Long.toString(tillStart) + " Minute";
            } else if (tillStart / 1000 > 0) {
                tillStart /= (1000);
                timeBetween = "Starts in " + Long.toString(tillStart) + " Second";
            } else {
                timeBetween = "Starts in " + Long.toString(tillStart) + " Millisecond";
            }

            if (tillStart > 1) // If its plural, then place an s at the end
                timeBetween += "s";

        } else { // Else, either the exam is running or it is done
            timeBetween = (tillEnd > 0) ? "Ends in " : "Ended ";
            String suffix = (tillEnd > 0) ? "" : " Ago";
            tillEnd = Math.abs(tillEnd);

            if (tillEnd / (1000 * 60 * 60 * 24 * 7) > 0) {
                tillEnd /= (1000 * 60 * 60 * 24 * 7);
                timeBetween += Long.toString(tillEnd) + " Week";
            } else if (tillEnd / (1000 * 60 * 60 * 24) > 0) {
                tillEnd /= (1000 * 60 * 60 * 24);
                timeBetween += Long.toString(tillEnd) + " Day";
            } else if (tillEnd / (1000 * 60 * 60) > 0) {
                tillEnd /= (1000 * 60 * 60);
                timeBetween += Long.toString(tillEnd) + " Hour";
            } else if (tillEnd / (1000 * 60) > 0) {
                tillEnd /= (1000 * 60);
                timeBetween += Long.toString(tillEnd) + " Minute";
            } else if (tillEnd / 1000 > 0) {
                tillEnd /= (1000);
                timeBetween += Long.toString(tillEnd) + " Second";
            } else {
                timeBetween += Long.toString(tillEnd) + " Millisecond";
            }

            if (tillEnd > 1)
                timeBetween += "s";
            timeBetween += suffix;
        }
        return timeBetween;
    }

    // Returns the number of exams in our list of exams
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
        Snackbar.make(getContext(), activity.findViewById(R.id.content), "Exam Deleted.", Snackbar.LENGTH_LONG).setAction("UNDO", v -> {
            examList.add(item); // Add to the local list
            db.insertExam(item); // Add to the database
            notifyItemInserted(examList.size() - 1); // Update the recycler view
        }).show();
    }

    // Delete all of the selected exams. If none are selected, then delete all exams
    public void deleteSelected() {
        if (selectedList.size() == 0) { // If there are no exams selected
            deleteAll(); // Delete all the exams
            return;
        }

        int size = selectedList.size(); // The number of exams selected
        for (int i = 0; i < size; i++) { // For all of the selected exams
            ExamModel item = selectedList.get(i); // Get the exam from the list
            examList.remove(item); // Remove the exam from the main list of all exams
            db.deleteExam(item.getExamID()); // Remove the exam from the database
        }

        selectedList.clear(); // Empty the list of selected exams
        notifyDataSetChanged(); // Tell the recycler view that elements were removed
    }

    // Delete all of the exams
    private void deleteAll() {
        int size = examList.size(); // The number of exams
        for (int i = 0; i < size; i++) { // For all the exams
            ExamModel item = examList.get(i); // Get the exam from the list
            db.deleteExam(item.getExamID()); // Remove the exam from the database
        }

        examList.clear(); // Empty the list of exams
        notifyItemRangeRemoved(0, size); // Tell the recycler view that exams were removed at the given position
    }

    // Edit the exam at the given index
    public void editExam(int index) {
        ExamModel exam = examList.get(index); // Get the exam at the given index
        Bundle bundle = new Bundle(); // Create a new bundle to hold our data (this is how we detect if we are editing or creating in the ExamEditor)
        bundle.putInt("id", exam.getExamID()); // Put the id in
        bundle.putString("name", exam.getName()); // Put the name in
        bundle.putString("unit", exam.getUnit()); // Put the unit in
        bundle.putString("date", exam.getDate()); // Put the date in
        bundle.putString("time", exam.getTime()); // Put the time in
        bundle.putString("location", exam.getLocation()); // Put the location in
        bundle.putFloat("duration", exam.getDuration()); // Put the duration in

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
