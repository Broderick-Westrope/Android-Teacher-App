package com.broderickwestrope.whiteboard.todolist.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.broderickwestrope.whiteboard.R;
import com.broderickwestrope.whiteboard.todolist.TodoActivity;
import com.broderickwestrope.whiteboard.todolist.Models.TaskModel;
import com.broderickwestrope.whiteboard.todolist.TaskEditor;
import com.broderickwestrope.whiteboard.todolist.Utils.DatabaseHandler;

import java.util.List;

// The wrapper/adapter between the database and the recycler view
public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {

    private List<TaskModel> taskList;
    private TodoActivity activity;
    private DatabaseHandler db;

    // Constructor
    public ToDoAdapter(DatabaseHandler db, TodoActivity activity) {
        this.activity = activity;
        this.db = db;
    }

    // Inflates (sets up) the list of tasks
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_layout, parent, false);
        return new ViewHolder(itemView);
    }

    // Sets up a view holder with data of a specified task (from the database)
    public void onBindViewHolder(ViewHolder holder, int index) {
        db.openDatabase(); // Open the task database for use
        TaskModel item = taskList.get(index); // Get the task from the list using the specified index

        holder.task.setText(item.getTask()); // Set the text to the task's text
        holder.task.setChecked(item.getStatus() != 0); // Convert the int to bool and set the status of the task

        String location = item.getLocation();
        if (location != null)
            holder.location.setText(location);
        else
            holder.location.setEnabled(false);

        // Listen for changes in the items' status
        holder.task.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Update the database item with the changed status
                db.updateStatus(item.getId(), (isChecked ? 1 : 0));
            }
        });
    }

    // Returns the number of tasks in our list of tasks (the to-do list)
    public int getItemCount() {
        return taskList.size();
    }

    // Set the local list of tasks to the given list of tasks
    public void setTasks(List<TaskModel> todoList) {
        this.taskList = todoList; // Set local to the given tasks
        notifyDataSetChanged(); // Notify any concerned members that the data has changed (this is mainly for updating the recycler view)
    }

    // Used to delete a task at the given index
    public void deleteItem(int index) {
        TaskModel item = taskList.get(index); // Get the item from the list
        db.deleteTask(item.getId()); // Remove the item from the database
        taskList.remove(index); // Remove the item from the list
        notifyItemRemoved(index); // Update the recycler view
        if (taskList.size() <= 0)
            activity.slideDeleteAll.setLocked(true);
    }

    // Used to delete all task items
    public void deleteAll() {
        int size = taskList.size();
        for (int i = 0; i < size; i++) {
            TaskModel item = taskList.get(i); // Get the item from the list
            db.deleteTask(item.getId()); // Remove the item from the database
        }

        taskList.clear(); // Remove the item from the list
        notifyItemRangeRemoved(0, size);
//        notifyItemRemoved(index); // Update the recycler view
    }

    // Edit the item at the given index
    public void editItem(int index) {
        TaskModel item = taskList.get(index); // Get the task at the given index
        Bundle bundle = new Bundle(); // Create a new bundle to hold our data
        bundle.putInt("id", item.getId()); // Put the ID in the bundle (this is how we detect if we are editing or creating in the TaskEditor)
        bundle.putString("task", item.getTask()); // Put the task name/description in the bundle
        bundle.putString("location", item.getLocation());
        TaskEditor fragment = new TaskEditor(); // Create a new TaskEditor fragment
        fragment.setArguments(bundle); // Put the bundle in the fragment
        fragment.show(activity.getSupportFragmentManager(), TaskEditor.TAG); // Display the fragment
    }

    public Context getContext() {
        return activity;
    }

    // Create a version of the RecyclerView ViewHolder with added checkbox
    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox task; // CheckBox for the status of our task
        TextView location;

        ViewHolder(View view) {
            super(view); // Execute the super of the function
            task = view.findViewById(R.id.taskCheckbox); // Set the checkbox to the one in task_layout
            location = view.findViewById(R.id.locationTxt); // Set the text view to the one in task_layout
        }
    }
}
