package com.broderickwestrope.whiteboard;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.broderickwestrope.whiteboard.Adapters.ToDoAdapter;
import com.broderickwestrope.whiteboard.Models.TaskModel;
import com.broderickwestrope.whiteboard.Utils.DatabaseHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

// This is our main to-do activity
public class MainActivity extends AppCompatActivity implements DialogCloseListener {

    private RecyclerView tasksRV; // This is the element that contains our list elements
    private ToDoAdapter tasksAdapter; // This is
    private FloatingActionButton fabAddTask;

    private List<TaskModel> taskList;
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        db = new DatabaseHandler(this);
        db.openDatabase();

        taskList = new ArrayList<>();

        tasksRV = findViewById(R.id.tasksRV);
        tasksRV.setLayoutManager(new LinearLayoutManager(this));
        tasksAdapter = new ToDoAdapter(db, this);
        tasksRV.setAdapter(tasksAdapter);

        fabAddTask = findViewById(R.id.fabAddTask);

        taskList = db.getAllTasks();
        Collections.reverse(taskList);
        tasksAdapter.setTasks(taskList);

        fabAddTask.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                TaskEditor.newInstance().show(getSupportFragmentManager(), TaskEditor.TAG);
            }
        });
    }

    @Override
    public void handleDialogClose(DialogInterface dialog) {
        taskList = db.getAllTasks();
        Collections.reverse(taskList);
        tasksAdapter.setTasks(taskList);
        tasksAdapter.notifyDataSetChanged();
    }
}