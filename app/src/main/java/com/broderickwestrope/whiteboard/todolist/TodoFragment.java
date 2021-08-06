package com.broderickwestrope.whiteboard.todolist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.broderickwestrope.whiteboard.R;
import com.broderickwestrope.whiteboard.todolist.Adapters.ToDoAdapter;
import com.broderickwestrope.whiteboard.todolist.Models.TaskModel;
import com.broderickwestrope.whiteboard.todolist.Utils.DatabaseHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ncorti.slidetoact.SlideToActView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

// This is our main to-do activity
public class TodoFragment extends AppCompatActivity implements DialogCloseListener {

    private RecyclerView tasksRV; // This is the element that contains our list elements
    private ToDoAdapter tasksAdapter; // This is
    private FloatingActionButton fabAddTask;
    public SlideToActView slideDeleteAll;

    private List<TaskModel> taskList;
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);
        Objects.requireNonNull(getSupportActionBar()).hide();

        db = new DatabaseHandler(this);
        db.openDatabase();

        slideDeleteAll = findViewById(R.id.slideDeleteAll);
        tasksRV = findViewById(R.id.tasksRV);
        tasksRV.setLayoutManager(new LinearLayoutManager(this));
        tasksAdapter = new ToDoAdapter(db, this);
        tasksRV.setAdapter(tasksAdapter);

        fabAddTask = findViewById(R.id.fabAddTask);
        ItemTouchHelper touchHelper = new ItemTouchHelper(new TaskTouchHelper(tasksAdapter));
        touchHelper.attachToRecyclerView(tasksRV);

        taskList = new ArrayList<>();
        taskList = db.getAllTasks();
        Collections.reverse(taskList);
        tasksAdapter.setTasks(taskList);
        slideDeleteAll.setLocked(taskList.isEmpty());

        fabAddTask.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                TaskEditor.newInstance().show(getSupportFragmentManager(), TaskEditor.TAG);
            }
        });

        slideDeleteAll.setOnSlideCompleteListener(new SlideToActView.OnSlideCompleteListener() {

            @Override
            public void onSlideComplete(SlideToActView slideToActView) {
                deleteAll();
                slideDeleteAll.resetSlider();
                slideDeleteAll.setLocked(true);
            }
        });
    }

    private void deleteAll()
    {
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
//                tasksAdapter.notifyDataSetChanged();
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
        slideDeleteAll.setLocked(taskList.isEmpty());
    }
}