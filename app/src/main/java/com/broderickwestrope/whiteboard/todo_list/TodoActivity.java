package com.broderickwestrope.whiteboard.todo_list;

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

import com.broderickwestrope.whiteboard.Interfaces.DialogCloseListener;
import com.broderickwestrope.whiteboard.R;
import com.broderickwestrope.whiteboard.Adapters.ToDoAdapter;
import com.broderickwestrope.whiteboard.Models.TaskModel;
import com.broderickwestrope.whiteboard.Utils.TaskDBManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

// This is our activity containing all of the elements for our to-do list
public class TodoActivity extends AppCompatActivity implements DialogCloseListener {

    public View deleteAllView; // The button allowing us to delete all the tasks
    private ToDoAdapter tasksAdapter; // The wrapper/adapter between the database and the recycler view
    private List<TaskModel> taskList; // A list of all of our tasks
    private TaskDBManager db; // Our database manager for the tasks (using SQLite)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        // Set the support action bar to our custom action bar with the title "Tasks"
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Tasks");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create our database manager and open it for use
        db = new TaskDBManager(this);
        db.openDatabase();

        // Get the view of our "delete all" button (the bin symbol)
        deleteAllView = findViewById(R.id.deleteAllAction);
        // Get the view of our "add task" button (the plus symbol)
        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);

        // Get the recycler view that contains our list of tasks
        RecyclerView tasksRV = findViewById(R.id.tasksRV);
        tasksRV.setLayoutManager(new LinearLayoutManager(this));
        tasksAdapter = new ToDoAdapter(db, this); // Create a new adapter
        tasksRV.setAdapter(tasksAdapter); // Attach the adapter to the recycler view

        // Create a new touch helper (this allows our swiping actions) and attach to the recycler view
        ItemTouchHelper touchHelper = new ItemTouchHelper(new TaskTouchHelper(tasksAdapter));
        touchHelper.attachToRecyclerView(tasksRV);


        taskList = new ArrayList<>(); // Create our local list of tasks
        taskList = db.getAllTasks(); // Assign any existing tasks from the database
        tasksAdapter.setTasks(taskList); // Set the recycler view to contain these tasks (using the adapter)

        // Set the onClick action for when a user wants to add a new task
        fabAddTask.setOnClickListener(v -> {
            // Open the task editor for a new task (the editor knows it is new because we pass no bundle)
            TaskEditor.newInstance().show(getSupportFragmentManager(), TaskEditor.TAG);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu. This adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tasks_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteAllAction: // When the delete all button is pressed
                if (!taskList.isEmpty()) // Only allow the user to delete when there are tasks present
                    deleteAll(); // Delete all tasks
                return true;
            case android.R.id.home: // When the back/home button (arrow) is pressed
                this.finish(); // Finish with the activity and return
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Our function to handle when the user wants to delete all the tasks
    private void deleteAll() {
        AlertDialog.Builder builder = new AlertDialog.Builder(tasksAdapter.getContext());
        builder.setTitle("Delete All Tasks"); // The title of the alert box
        builder.setMessage("Are you sure you want to delete all tasks?\nThis action cannot be undone"); // The content of the alert box
        // The positive button action
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> tasksAdapter.deleteAll());

        // The negative button action
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {
        });
        AlertDialog dialog = builder.create(); // Build the alert
        dialog.show(); // Display the alert
    }

    //Performed every time a dialogue is closed. We use this to refresh when the task editor cancels or saves
    @Override
    public void handleDialogClose(DialogInterface dialog) {
        taskList = db.getAllTasks(); // Get all tasks from the database
        tasksAdapter.setTasks(taskList); // Set the recycler view tasks using the adapter
        tasksAdapter.notifyDataSetChanged(); // Notify that the data set has changed. This refreshes the recyclerview
    }
}