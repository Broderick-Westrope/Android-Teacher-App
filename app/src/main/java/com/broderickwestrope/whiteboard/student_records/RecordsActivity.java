package com.broderickwestrope.whiteboard.student_records;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.broderickwestrope.whiteboard.R;
import com.broderickwestrope.whiteboard.student_records.Adapters.RecordAdapter;
import com.broderickwestrope.whiteboard.student_records.Models.RecordModel;
import com.broderickwestrope.whiteboard.student_records.Utils.RecordDBManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

// This is our activity containing all of the elements for our to-do list
public class RecordsActivity extends AppCompatActivity implements DialogCloseListener {

    public View deleteAllView; // The button allowing us to delete all the records
    private RecordAdapter recordsAdapter; // The wrapper/adapter between the database and the recycler view
    private List<RecordModel> recordList; // A list of all of our records
    private RecordDBManager db; // Our database manager for the records (using SQLite)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        // Set the support action bar to our custom action bar with the title "Records"
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Student Records");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create our database manager and open it for use
        db = new RecordDBManager(this);
        db.openDatabase();

        // Get the view of our "delete all" button (the bin symbol)
        deleteAllView = findViewById(R.id.deleteAllAction);
        // Get the view of our "add record" button (the plus symbol)
        FloatingActionButton fabAddRecord = findViewById(R.id.fabAddRecord);

        // Get the recycler view that contains our list of records
        RecyclerView recordsRV = findViewById(R.id.recordsRV);
        recordsRV.setLayoutManager(new LinearLayoutManager(this));
        recordsAdapter = new RecordAdapter(db, this); // Create a new adapter
        recordsRV.setAdapter(recordsAdapter); // Attach the adapter to the recycler view

        // Create a new touch helper (this allows our swiping actions) and attach to the recycler view
        ItemTouchHelper touchHelper = new ItemTouchHelper(new RecordTouchHelper(recordsAdapter));
        touchHelper.attachToRecyclerView(recordsRV);

        recordList = new ArrayList<>(); // Create our local list of records
        recordList = db.getAllRecords(); // Assign any existing records from the database
        recordsAdapter.setRecords(recordList); // Set the recycler view to contain these records (using the adapter)

        // Set the onClick action for when a user wants to add a new record
        fabAddRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the record editor for a new record (the editor knows it is new because we pass no bundle)
                new RecordEditor(RecordsActivity.this).show(getSupportFragmentManager(), RecordEditor.TAG);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu. This adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.records_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteAllAction: // When the delete all button is pressed
                if (!recordList.isEmpty()) // Only allow the user to delete when there are records present
                    deleteAll(); // Delete all records
                return true;
            case android.R.id.home: // When the back/home button (arrow) is pressed
                this.finish(); // Finish with the activity and return
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Our function to handle when the user wants to delete all the records
    private void deleteAll() {
        AlertDialog.Builder builder = new AlertDialog.Builder(recordsAdapter.getContext());
        builder.setTitle("Delete All Records"); // The title of the alert box
        builder.setMessage("Are you sure you want to delete all records?\nThis action cannot be undone"); // The content of the alert box
        // The positive button action
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                recordsAdapter.deleteAll();
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

    //Performed every time a dialogue is closed. We use this to refresh when the record editor cancels or saves
    @Override
    public void handleDialogClose(DialogInterface dialog) {
        recordList = db.getAllRecords(); // Get all records from the database
        recordsAdapter.setRecords(recordList); // Set the recycler view records using the adapter
        recordsAdapter.notifyDataSetChanged(); // Notify that the data set has changed. This refreshes the recyclerview
    }
}