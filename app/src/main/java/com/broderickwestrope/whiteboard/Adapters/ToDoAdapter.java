package com.broderickwestrope.whiteboard.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.broderickwestrope.whiteboard.Editors.TaskEditor;
import com.broderickwestrope.whiteboard.Models.TaskModel;
import com.broderickwestrope.whiteboard.R;
import com.broderickwestrope.whiteboard.Utils.TaskDBManager;
import com.broderickwestrope.whiteboard.todo_list.TodoActivity;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Random;

// The wrapper/adapter between the database and the recycler view
public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {

    private List<TaskModel> taskList; // A list of all of our tasks
    private TodoActivity activity; // The activity that is using this adapter to display the tasks
    private TaskDBManager db;  // Our database manager for the tasks (using SQLite)

    // Class constructor
    public ToDoAdapter(TaskDBManager db, TodoActivity activity) {
        this.activity = activity; // Set the containing activity
        this.db = db; // Set the database being used
    }

    // Inflates (ie. sets up) the given task/card view
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new ViewHolder(itemView);
    }

    // Sets up the contents of a view holder with the data of a specified task (from the database)
    public void onBindViewHolder(ViewHolder holder, int index) {
        db.openDatabase(); // Open the task database for use
        TaskModel item = taskList.get(index); // Get the task from the list using the specified index

        holder.task.setText(item.getTask()); // Set the text to the task's text
        holder.task.setChecked(item.getStatus() != 0); // Convert the int to bool and set the status of the task

        // Select a random color for card background (from the given array) :)
        setRandomColor(holder.card);

        // Check if the location was set (since this is optional) and only display it if it has contents
        String location = item.getLocation(); // Get the location
        if (location != null) // If its null
            holder.location.setText(location); // Set the text
        else // Else, disable the element
            holder.location.setEnabled(false);

        // Listen for changes in the checkbox
        holder.task.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Update the status of the database item
            db.updateStatus(item.getId(), (isChecked ? 1 : 0));
        });

        // Listen for clicks on the 'card'
        holder.card.setOnClickListener(v -> {
            // Toggle the checkbox
            holder.task.setChecked(!holder.task.isChecked());
        });

        // Listen for long-clicks on the 'card'
        holder.card.setOnLongClickListener((v) -> {
            // Select a random color for card background (from the given array) :)
            setRandomColor(holder.card);
            return false;
        });
    }

    // Select a random color for card background (from the given array) :)
    private void setRandomColor(View v) {
        Random random = new Random(); // Create a new instance of random
        String[] colorArray = getContext().getResources().getStringArray(R.array.card_colors); // Get the array of colors
        String randomColorName = colorArray[random.nextInt(colorArray.length)]; // Select a random color from the array
        v.setBackgroundColor(Color.parseColor(randomColorName)); // Set the background to the random color
    }

    // Returns the number of tasks in our list of tasks (ie. the length of the to-do list)
    public int getItemCount() {
        return taskList.size();
    }

    // Set the local list of tasks to the given list of tasks
    public void setTasks(List<TaskModel> todoList) {
        this.taskList = todoList; // Set the list of tasks
        notifyDataSetChanged(); // Notify any concerned members that the data has changed (this is mainly for updating the recycler view)
    }

    // Used to delete a task at the given index
    public void deleteItem(int index) {
        TaskModel item = taskList.get(index); // Get the item from the list
        db.deleteTask(item.getId()); // Remove the item from the database
        taskList.remove(index); // Remove the item from the list
        notifyItemRemoved(index); // Update the recycler view

        // Create a snackbar to say that the entry was deleted and allow the user to undo this if it was a mistake
        Snackbar.make(getContext(), activity.findViewById(R.id.content), "Task Deleted.", Snackbar.LENGTH_LONG).setAction("UNDO", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskList.add(item); // Add to the local list
                db.insertTask(item); // Add to the database
                notifyItemInserted(taskList.size() - 1); // Update the recycler view
            }
        }).show(); // Display the message

    }

    // Used to delete all task items
    public void deleteAll() {
        int size = taskList.size();
        for (int i = 0; i < size; i++) {
            TaskModel item = taskList.get(i); // Get the item from the list
            db.deleteTask(item.getId()); // Remove the item from the database
        }

        taskList.clear(); // Remove the item from the list
        notifyItemRangeRemoved(0, size); // Tell the recycler view that elements were removed at the given position
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

    // Returns the current activity
    public Context getContext() {
        return activity;
    }

    // Create a version of the RecyclerView ViewHolder with added views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox task; // CheckBox for the status of our task as well as the text
        TextView location; // TextView for the location of our task
        RelativeLayout card; // A "card" that everything is displayed on (this is so we can change the color)

        ViewHolder(View view) {
            super(view); // Execute the base function
            task = view.findViewById(R.id.taskCheckbox); // Set the checkbox to the one in task_layout
            location = view.findViewById(R.id.locationTxt); // Set the text view to the one in task_layout
            card = view.findViewById(R.id.layoutCard); // Set the relative layout to the one in task_layout
        }
    }
}
