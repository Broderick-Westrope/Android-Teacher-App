package com.broderickwestrope.whiteboard.student_records.Adapters;

import android.app.Activity;
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
import com.broderickwestrope.whiteboard.exams.ViewRecordActivity;
import com.broderickwestrope.whiteboard.student_records.Models.RecordModel;
import com.broderickwestrope.whiteboard.student_records.RecordEditor;
import com.broderickwestrope.whiteboard.student_records.RecordsActivity;
import com.broderickwestrope.whiteboard.student_records.Utils.RecordDBManager;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Random;

// The wrapper/adapter between the database and the recycler view
public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {

    private final com.broderickwestrope.whiteboard.exams.Utils.ExamDBManager examDB;  // The database manager for the exams (using SQLite)
    private List<RecordModel> recordList; // A list of all of our records
    private Activity activity; // The activity that is using this adapter to display the records
    private RecordDBManager db;  // Our database manager for the records (using SQLite)


    // Class constructor
    public RecordAdapter(RecordDBManager db, Activity activity, com.broderickwestrope.whiteboard.exams.Utils.ExamDBManager examDB) {
        this.activity = activity; // Set the containing activity
        this.db = db; // Set the database being used
        this.examDB = examDB; // Set the exam database being used
    }

    // Inflates (ie. sets up) the given record/card view
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record, parent, false);
        return new ViewHolder(itemView);
    }

    // Sets up the contents of a view holder with the data of a specified record (from the database)
    public void onBindViewHolder(ViewHolder holder, int index) {
        db.openDatabase(); // Open the record database for use
        RecordModel item = recordList.get(index); // Get the record from the list using the specified index

        holder.studentID.setText(String.valueOf(item.getId())); // Set the Student ID to the record's ID
        holder.name.setText(item.getName()); // Set the name to the student's name
        holder.course.setText(item.getCourse()); // Set the course to the student's course

        // Select a random color for card background (from the given array) :)
        Random random = new Random();
        String[] colorArray = getContext().getResources().getStringArray(R.array.card_colors);
        String randomColorName = colorArray[random.nextInt(colorArray.length)];
        holder.card.setBackgroundColor(Color.parseColor(randomColorName));

        // Listen for clicks on the student record to see more about the
        // record including the students information and their exams
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Used to change activity to view all information about the selected record
                viewRecord(item);
            }
        });
    }

    // Changes activity to see more about the record including the students information and their exams
    public void viewRecord(RecordModel record) {
        // Create a new intend and bundle to go to the new activity and pass the data to it
        Intent i = new Intent(activity, ViewRecordActivity.class);
        Bundle b = new Bundle();

        // Put all the information in the bundle for the next activity
        b.putInt("id", record.getId()); // Put in the student ID
        b.putString("name", record.getName()); // Put in the name
        b.putString("gender", record.getGender()); // Put in the gender
        b.putString("course", record.getCourse()); // Put in the course
        b.putInt("age", record.getAge()); // Put in the age
        b.putString("address", record.getAddress()); // Put in the address
        b.putByteArray("image", record.getImage()); // Put the image in

        //Put the bundle of extras in the intent and start the activity
        i.putExtras(b);
        activity.startActivity(i);
    }

    // Returns the number of records in our list of records (ie. the length of the to-do list)
    public int getItemCount() {
        return recordList.size();
    }

    // Set the local list of records to the given list of records
    public void setRecords(List<RecordModel> todoList) {
        this.recordList = todoList; // Set local to the given records
        notifyDataSetChanged(); // Notify any concerned members that the data has changed (this is mainly for updating the recycler view)
    }

    // Used to delete a record at the given index
    public void deleteItem(int index) {
        RecordModel item = recordList.get(index); // Get the item from the list
        db.deleteRecord(item.getId()); // Remove the item from the database
        examDB.deleteStudent(item.getId()); //Remove all the exams of this student
        recordList.remove(index); // Remove the item from the list
        notifyItemRemoved(index); // Update the recycler view

        // Create a snackbar to say that the entry was deleted and allow the user to undo this if it was a mistake
        Snackbar.make(getContext(), activity.findViewById(R.id.content), "Record Deleted.", Snackbar.LENGTH_LONG).setAction("UNDO", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordList.add(item); // Add to the local list
                db.insertRecord(item); // Add to the database
                notifyItemInserted(recordList.size() - 1); // Update the recycler view
            }
        }).show();
    }

    // Used to delete all record items
    public void deleteAll() {
        //Delete all records
        int size = recordList.size();
        for (int i = 0; i < size; i++) {
            RecordModel item = recordList.get(i); // Get the item from the list
            db.deleteRecord(item.getId()); // Remove the item from the database
        }
        recordList.clear(); // Remove the item from the list
        notifyItemRangeRemoved(0, size); // Tell the recycler view that elements were removed at the given position

        //Delete all exams
        examDB.deleteAll();
    }

    // Edit the item at the given index
    public void editRecord(int index) {
        RecordModel item = recordList.get(index); // Get the record at the given index
        Bundle bundle = new Bundle(); // Create a new bundle to hold our data (this is how we detect if we are editing or creating in the RecordEditor)
        bundle.putInt("id", item.getId()); // Put the ID in the bundle
        bundle.putString("name", item.getName()); // Put the name in the bundle
        bundle.putString("gender", item.getGender()); // Put the gender in the bundle
        bundle.putString("course", item.getCourse()); // Put the course in the bundle
        bundle.putInt("age", item.getAge()); // Put the age in the bundle
        bundle.putString("address", item.getAddress()); // Put the address in the bundle
        bundle.putByteArray("image", item.getImage()); // Put the image in the bundle

        RecordEditor fragment = new RecordEditor(activity); // Create a new RecordEditor fragment
        fragment.setArguments(bundle); // Put the bundle in the fragment
        fragment.show(((RecordsActivity) activity).getSupportFragmentManager(), RecordEditor.TAG); // Display the fragment
    }

    // Returns the current activity
    public Context getContext() {
        return activity;
    }

    // Create a version of the RecyclerView ViewHolder with added views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView studentID, name, course; // TextViews for remaining components of the student record
        RelativeLayout card; // A "card" that everything is displayed on (this is so we can change the color)

        ViewHolder(View view) {
            super(view); // Execute the base function
            studentID = view.findViewById(R.id.record_StudentID); // Set the text view to the one in record_layout
            name = view.findViewById(R.id.record_Name); // Set the text view to the one in record_layout
            course = view.findViewById(R.id.record_Course); // Set the text view to the one in record_layout
            card = view.findViewById(R.id.layoutCard); // Set the relative layout to the one in record_layout
        }
    }
}
