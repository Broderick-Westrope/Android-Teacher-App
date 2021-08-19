package com.broderickwestrope.whiteboard.todolist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.broderickwestrope.whiteboard.R;
import com.broderickwestrope.whiteboard.todolist.Adapters.ToDoAdapter;
import com.broderickwestrope.whiteboard.todolist.Models.TaskModel;
import com.broderickwestrope.whiteboard.todolist.Utils.DatabaseHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// This is our main to-do activity
public class TodoActivity extends AppCompatActivity implements DialogCloseListener {

    private ToDoAdapter tasksAdapter;
    private List<TaskModel> taskList;
    private DatabaseHandler db;

    public View deleteAllView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        // Set up the support action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Tasks");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);

        db = new DatabaseHandler(this);
        db.openDatabase();


        deleteAllView = findViewById(R.id.deleteAllAction);
        // This is the element that contains our list elements
        RecyclerView tasksRV = findViewById(R.id.tasksRV);
        tasksRV.setLayoutManager(new LinearLayoutManager(this));
        tasksAdapter = new ToDoAdapter(db, this);
        tasksRV.setAdapter(tasksAdapter);

        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);
        ItemTouchHelper touchHelper = new ItemTouchHelper(new TaskTouchHelper(tasksAdapter));
        touchHelper.attachToRecyclerView(tasksRV);

        taskList = new ArrayList<>();
        taskList = db.getAllTasks();
        Collections.reverse(taskList);
        tasksAdapter.setTasks(taskList);

//        deleteAllView.setEnabled(false);

        fabAddTask.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                TaskEditor.newInstance().show(getSupportFragmentManager(), TaskEditor.TAG);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tasks_menu, menu);
        return true;
    }

    // Go back home on arrow press
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteAllAction:
                deleteAll();
                return true;
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAll() {
        AlertDialog.Builder builder = new AlertDialog.Builder(tasksAdapter.getContext()); //has no contents!!!!!!!!!!!!!!!
        builder.setTitle("Delete All Tasks");
        builder.setMessage("Are you sure you want to delete all tasks?");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                tasksAdapter.deleteAll();
            }
        });

        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void handleDialogClose(DialogInterface dialog) {
        taskList = db.getAllTasks();
        Collections.reverse(taskList);
        tasksAdapter.setTasks(taskList);
        tasksAdapter.notifyDataSetChanged();
        deleteAllView.setEnabled(!taskList.isEmpty());
    }
}