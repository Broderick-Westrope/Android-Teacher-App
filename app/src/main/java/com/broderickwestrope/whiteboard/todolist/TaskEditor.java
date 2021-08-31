package com.broderickwestrope.whiteboard.todolist;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.broderickwestrope.whiteboard.R;
import com.broderickwestrope.whiteboard.todolist.Models.TaskModel;
import com.broderickwestrope.whiteboard.todolist.Utils.TaskDBManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Objects;

// This provides the interface on the lower-portion of the screen to create and edit the contents of tasks
public class TaskEditor extends BottomSheetDialogFragment {

    // Store the type of dialogue for easy access outside of the class
    public static final String TAG = "ActionBottomDialog";

    // Assign colors for when the save button is either enabled or disabled (minimising inconsistencies)
    int enabledColor = R.color.mirage2;
    int disabledColor = R.color.geyser;

    // Our database manager for the tasks (using SQLite)
    private TaskDBManager db;

    // Views within our fragment:
    private TextView changeTitleTxt; // The title either reading "Edit Task" or "New Task"
    private EditText editTaskTxt; // The field for the task name/description input
    private EditText editLocTxt; // The field for the location input
    private Button saveTaskBtn; // The button to save the changes to the task

    // For creating a new instance of this fragment
    public static TaskEditor newInstance() {
        return new TaskEditor();
    }

    // Add our custom style to the on create method
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.EditTaskStyle);
    }

    // Set soft input mode as this allows us to move the items up when the keyboard opens up (when editing the task details)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_task, container, false);
        Objects.requireNonNull(getDialog()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Collect the references to the views
        changeTitleTxt = requireView().findViewById(R.id.changeTitleTxt);
        editTaskTxt = requireView().findViewById(R.id.newTaskTxt);
        editLocTxt = requireView().findViewById(R.id.newTaskLocTxt);
        saveTaskBtn = requireView().findViewById(R.id.saveTaskBtn);

        // Used to differentiate between when we are trying to create a new task or update an existing task
        boolean isUpdate = false;

        // The bundle lets us get any data passed to this fragment
        final Bundle bundle = getArguments();
        if (bundle != null) // If our bundle isn't empty (ie. we were passed data)
        {
            // If we were passed data then that means the user has selected a task from the list to edit
            isUpdate = true; // This means we are updating an existing task
            String task = bundle.getString("task"); // Get the task text
            String location = bundle.getString("location"); // Get the location text

            changeTitleTxt.setText("Edit Task"); // Display that the user is editing an existing task
            editTaskTxt.setText(task); // Display the existing task in the input field
            editLocTxt.setText(location); // Display the existing location in the input field

            // Make sure the task is not null
            assert task != null;
            if (task.length() > 0) {
                // Set the color of the button to red (showing it is enabled)
                saveTaskBtn.setEnabled(true);
                saveTaskBtn.setTextColor(ContextCompat.getColor(requireContext(), enabledColor));
            }
        } else { // Else, if weren't passed any data, then we are creating a new task
            changeTitleTxt.setText("New Task"); // Display that the user is creating a new task
        }

        db = new TaskDBManager(getActivity()); // Create a new database handler
        db.openDatabase(); // Open the database for use

        // Add a listener for changes in the task's text
        editTaskTxt.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            // When the text is changed
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) { // If it is now empty
                    saveTaskBtn.setEnabled(false); //Disable the button
                    saveTaskBtn.setTextColor(ContextCompat.getColor(requireContext(), disabledColor)); // Set the color to gray
                } else { // If it is not empty
                    saveTaskBtn.setEnabled(true); //Enable the button
                    saveTaskBtn.setTextColor(ContextCompat.getColor(requireContext(), enabledColor)); // Set the enabled color
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Listen for clicks on the save button
        boolean finalIsUpdate = isUpdate; // Copy of our update value so we can use it safely within the onClick method
        saveTaskBtn.setOnClickListener(v -> {
            String taskText = editTaskTxt.getText().toString(); // Get the string of the task
            String location = editLocTxt.getText().toString(); // Get the string of the location

            if (finalIsUpdate) { // If we are updating an existing task
                db.updateTask(bundle.getInt("id"), taskText, location); // Update the text of the task
            } else { // Else, if we are adding a new task
                TaskModel task = new TaskModel(); // Create a new task
                task.setTask(taskText); // Set the task text
                task.setLocation(location); // Set the location
                task.setStatus(0); // Set it to incomplete by default

                db.insertTask(task); // Insert the task to the database
            }
            dismiss(); // Dismiss the task editor fragment (this)
        });
    }

    // Allows us to refresh/update the recyclerview of our tasks each time the panel is dismissed (when the task is saved)
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        Activity activity = getActivity(); // Get a reference to the current activity
        if (activity instanceof DialogCloseListener) { // If this activity is an instance of our custom listener
            ((DialogCloseListener) activity).handleDialogClose(dialog); // Handle the close
        }
    }
}
