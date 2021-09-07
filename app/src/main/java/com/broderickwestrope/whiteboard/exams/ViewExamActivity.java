package com.broderickwestrope.whiteboard.exams;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.broderickwestrope.whiteboard.R;
import com.broderickwestrope.whiteboard.exams.Models.ExamModel;

public class ViewExamActivity extends AppCompatActivity {

    String timeBetween; //Stores the time between now and the exams start or end (whichever is closer)
    private ExamModel exam; //This is the exam that was selected

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_exam);

        // Set the support action bar to our custom action bar with the title "Student Record"
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Exam Information");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Collect and display this students data
        setupRecord();
    }

    // Creates the record with the data from the passed bundle and then displays the values in the views
    private void setupRecord() {
        // Collect all the data that was given to the activity through the bundle
        // (This is how we know what student we are displaying information for)
        Bundle b = getIntent().getExtras(); // Collect the bundle
        exam = new ExamModel(); // Initialise the record
        exam.setName(b.getString("name")); // Set the exams name
        exam.setUnit(b.getString("unit")); // Set the unit the exam is for
        exam.setDate(b.getString("date")); // Set the date of the exam
        exam.setTime(b.getString("time")); // Set the time of the exam
        exam.setLocation(b.getString("name")); // Set the exams location
        exam.setDuration(b.getFloat("duration")); // Set the exams duration

        // Set the corresponding text views to the data we were passed in the bundle
        ((TextView) findViewById(R.id.exam_Name)).setText(exam.getName()); // Display the name
        ((TextView) findViewById(R.id.exam_Unit)).setText(exam.getUnit()); // Display the name
        ((TextView) findViewById(R.id.exam_Date)).setText(exam.getDate()); // Display the name
        ((TextView) findViewById(R.id.exam_Time)).setText(exam.getTime()); // Display the name
        ((TextView) findViewById(R.id.exam_Location)).setText(exam.getLocation()); // Display the name
        ((TextView) findViewById(R.id.exam_Duration)).setText(String.valueOf(exam.getDuration())); // Display the name

        if (b.containsKey("time between")) { // If the time between has been passed to the activity via the extras
            timeBetween = b.getString("time between"); // Get the time between
            ((TextView) findViewById(R.id.exam_TimeBetween)).setText(timeBetween); // Display the time between
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu. This adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_exam_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: // When the back/home button (arrow) is pressed
                this.finish(); // Finish with the activity and return
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}