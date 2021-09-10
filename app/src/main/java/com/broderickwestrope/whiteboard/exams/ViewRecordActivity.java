package com.broderickwestrope.whiteboard.exams;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.broderickwestrope.whiteboard.R;
import com.broderickwestrope.whiteboard.exams.Adapters.ExamAdapter;
import com.broderickwestrope.whiteboard.exams.Listeners.DialogCloseListener;
import com.broderickwestrope.whiteboard.exams.Models.ExamModel;
import com.broderickwestrope.whiteboard.exams.Utils.ExamDBManager;
import com.broderickwestrope.whiteboard.student_records.MapsActivity;
import com.broderickwestrope.whiteboard.student_records.Models.RecordModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

// This activity is used to view the selected student record. It is categorised as more to do with exams
// because the list of exams on this page makes up most of the functionality
public class ViewRecordActivity extends AppCompatActivity implements DialogCloseListener {

    public View deleteAllView; // The button allowing us to delete all the exams
    private ExamAdapter examsAdapter; // The wrapper/adapter between the database and the recycler view
    private List<ExamModel> examList; // A list of all of our exams
    private ExamDBManager db; // Our database manager for the exams (using SQLite)
    private RecordModel studentRecord; //This is the record of the student that was selected

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_record);

        // Set the support action bar to our custom action bar with the title "Student Record"
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Student Record");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Collect and display this students data
        setupRecord();

        // Create our database manager and open it for use
        db = new ExamDBManager(this);
        db.openDatabase();

        // Set the references to the views of the buttons
        deleteAllView = findViewById(R.id.deleteAllAction); // Get the view of our "delete all" button (the bin symbol)
        FloatingActionButton fabAddExam = findViewById(R.id.fabAddExam); // Get the view of our "add exam" button (the plus symbol)

        // Get the recycler view that contains our list of exams
        RecyclerView examsRV = findViewById(R.id.examsRV);
        examsRV.setLayoutManager(new LinearLayoutManager(this));
        examsAdapter = new ExamAdapter(db, this); // Create a new adapter
        examsRV.setAdapter(examsAdapter); // Attach the adapter to the recycler view

        // Create a new touch helper (this allows our swiping actions) and attach to the recycler view
        ItemTouchHelper touchHelper = new ItemTouchHelper(new ExamTouchHelper(examsAdapter));
        touchHelper.attachToRecyclerView(examsRV);

        // Set the list of exams
        examList = new ArrayList<>(); // Create our local list of exams
        examList = db.getStudentExams(studentRecord.getId()); // Assign any existing exams from the database
        examsAdapter.setExams(examList); // Set the recycler view to contain these exams (using the adapter)

        // Set the onClick action for when a user wants to add a new exam
        fabAddExam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the exam editor for a new exam (the editor knows it is new because we pass no bundle)
                new ExamEditor(ViewRecordActivity.this, studentRecord.getId()).show(getSupportFragmentManager(), ExamEditor.TAG);
            }
        });
    }

    // Creates the record with the data from the passed bundle and then displays the values in the views
    private void setupRecord() {
        // Collect all the data that was given to the activity through the bundle
        // (This is how we know what student we are displaying information for)
        Bundle b = getIntent().getExtras(); // Collect the bundle
        studentRecord = new RecordModel(); // Initialise the record
        studentRecord.setId(b.getInt("id")); // Set the student ID
        studentRecord.setName(b.getString("name")); // Set the students name
        studentRecord.setGender(b.getString("gender")); // Set the students gender
        studentRecord.setCourse(b.getString("course")); // Set the students course
        studentRecord.setAge(b.getInt("age")); // Set the students age
        String address = b.getString("address"); // Store the address cause we also use it later (for showing the map)
        studentRecord.setAddress(address); // Set the students address
        studentRecord.setImage(b.getByteArray("image")); // Set the students image

        // Set the corresponding text views to the data we were passed in the bundle
        ((TextView) findViewById(R.id.record_StudentID)).setText(String.valueOf(studentRecord.getId())); // Display their student ID
        ((TextView) findViewById(R.id.record_Name)).setText(studentRecord.getName()); // Display their name
        ((TextView) findViewById(R.id.record_Gender)).setText(studentRecord.getGender()); // Display their gender
        ((TextView) findViewById(R.id.record_Course)).setText(studentRecord.getCourse()); // Display their course
        ((TextView) findViewById(R.id.record_Age)).setText(String.valueOf(studentRecord.getAge())); // Display their age
        ((TextView) findViewById(R.id.record_Address)).setText(studentRecord.getAddress()); // Display their address
        ((ImageView) findViewById(R.id.record_Image)).setImageBitmap(byteArrayToBitmap(studentRecord.getImage())); // Display their image

        // Set the onclick for seeing the address on the google-maps activity
        ((Button) findViewById(R.id.record_SeeMapBtn)).setOnClickListener(v -> {
            Intent i = new Intent(ViewRecordActivity.this, MapsActivity.class);
            Bundle mapsBundle = new Bundle();
            mapsBundle.putString("address", address);
            i.putExtras(mapsBundle);
            startActivity(i);
        });
    }

    // Converts the byte-array to a bitmap image that we can display
    private Bitmap byteArrayToBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu. This adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.exams_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteAllAction: // When the delete all button is pressed
                if (!examList.isEmpty()) // Only allow the user to delete when there are exams present
                    deleteAll(); // Delete all exams
                return true;
            case android.R.id.home: // When the back/home button (arrow) is pressed
                this.finish(); // Finish with the activity and return
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Our function to handle when the user wants to delete all the exams
    private void deleteAll() {
        AlertDialog.Builder builder = new AlertDialog.Builder(examsAdapter.getContext());
        builder.setTitle("Delete All Exams"); // The title of the alert box
        builder.setMessage("Are you sure you want to delete all exams?\nThis action cannot be undone"); // The content of the alert box
        // The positive button action
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                examsAdapter.deleteAll();
            }
        });

        // The positive button action
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create(); // Build the alert
        dialog.show(); // Display the alert
    }

    //Performed every time a dialogue is closed. We use this to refresh when the exam editor cancels or saves
    @Override
    public void handleDialogClose(DialogInterface dialog) {
        examList = db.getStudentExams(studentRecord.getId()); // Get all exams from the database
        examsAdapter.setExams(examList); // Set the recycler view exams using the adapter
        examsAdapter.notifyDataSetChanged(); // Notify that the data set has changed. This refreshes the recyclerview
    }
}