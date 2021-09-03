package com.broderickwestrope.whiteboard.exams;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.broderickwestrope.whiteboard.R;
import com.broderickwestrope.whiteboard.exams.Models.ExamModel;
import com.broderickwestrope.whiteboard.exams.Utils.ExamDBManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;
import java.util.Objects;

// This provides the interface on the lower-portion of the screen to create and edit the contents of exams
public class ExamEditor extends BottomSheetDialogFragment {

    // Store the type of dialogue for easy access outside of the class
    public static final String TAG = "ActionBottomDialog";

    // Assign colors for when the save button is either enabled or disabled (minimising inconsistencies)
    int enabledColor = R.color.mirage2;
    int disabledColor = R.color.geyser;

    int studentID;

    // Our database manager for the exams (using SQLite)
    private ExamDBManager db;

    // Holds the instance of the activity
    private ViewRecordActivity activity;

    // Views within our fragment:
    private TextView changeTitleTxt; // The title either reading "Edit Exam" or "New Exam"
    private EditText editExam_Name; // The field for the exam name input
    private EditText editExam_Unit; // The field for the exam unit/subject input
    private TextView editExam_Date; // The field for the exam date input
    private TextView editExam_Time; // The field for the exam time input
    private EditText editExam_Location; // The field for the exam location input
    private EditText editExam_Duration; // The field for the exam duration input
    private Button saveExamBtn; // The button to save the changes to the exam

    //TODO COmment this better. Its stuff for our date picker dialog
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private TimePickerDialog.OnTimeSetListener timeSetListener;

    // Class constructor
    public ExamEditor(ViewRecordActivity activity, int studentID) {
        this.activity = activity;
        this.studentID = studentID;
    }

    // Add our custom style to the on create method
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.EditExamStyle);
    }

    // Set soft input mode as this allows us to move the items up when the keyboard opens up (when editing the exam details)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_exam, container, false);
        Objects.requireNonNull(getDialog()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Collect the references to the views
        changeTitleTxt = requireView().findViewById(R.id.editExam_Title);
        editExam_Name = requireView().findViewById(R.id.editExam_Name);
        editExam_Unit = requireView().findViewById(R.id.editExam_Unit);
        editExam_Date = requireView().findViewById(R.id.editExam_Date);
        editExam_Time = requireView().findViewById(R.id.editExam_Time);
        editExam_Location = requireView().findViewById(R.id.editExam_Location);
        editExam_Duration = requireView().findViewById(R.id.editExam_Duration);
        saveExamBtn = requireView().findViewById(R.id.saveExamBtn);

        // Used to differentiate between when we are trying to create a new exam or update an existing exam
        boolean isUpdate = false;

        // Set the color of the button to red (showing it is enabled)
        saveExamBtn.setEnabled(true);
        saveExamBtn.setTextColor(ContextCompat.getColor(requireContext(), enabledColor));

        // The bundle lets us get any data passed to this fragment
        final Bundle bundle = getArguments();
        if (bundle != null) // If our bundle isn't empty (ie. we were passed data)
        {
            // If we were passed data then that means the user has selected a exam from the list to edit
            isUpdate = true; // This means we are updating an existing exam
            String name = bundle.getString("name"); // Get the exam name
            String unit = bundle.getString("unit"); // Get the exam unit
            String date = bundle.getString("date"); // Get the exam date
            String time = bundle.getString("time"); // Get the exam time
            String location = bundle.getString("location"); // Get the exam location
            float duration = bundle.getFloat("duration"); // Get the exam duration

            changeTitleTxt.setText("Edit Exam"); // Display that the user is editing an existing exam
            editExam_Name.setText(name); // Display the existing name in the input field
            editExam_Unit.setText(unit); // Display the existing name in the input field
            editExam_Date.setText(date); // Display the existing name in the input field
            editExam_Time.setText(time); // Display the existing name in the input field
            editExam_Location.setText(location); // Display the existing name in the input field
            editExam_Duration.setText(String.valueOf(duration)); // Display the existing name in the input field
        } else { // Else, if weren't passed any data, then we are creating a new exam
            changeTitleTxt.setText("New Exam"); // Display that the user is creating a new exam
        }

        db = new ExamDBManager(getActivity()); // Create a new database handler
        db.openDatabase(); // Open the database for use

        // Listen for clicks on the save button
        boolean finalIsUpdate = isUpdate; // Copy of our update value so we can use it safely within the onClick method
        saveExamBtn.setOnClickListener(v -> {
            if (!canSaveExam(editExam_Name, editExam_Unit, editExam_Date, editExam_Time, editExam_Location, editExam_Duration))
                return;

            // Get the corresponding values from each of the input views
            String name = editExam_Name.getText().toString(); // Get the exam name
            String unit = editExam_Unit.getText().toString(); // Get the unit
            String date = editExam_Date.getText().toString(); // Get the date
            String time = editExam_Time.getText().toString(); // Get the time of the exam
            String location = editExam_Location.getText().toString(); // Get the location of the exam
            float duration = Float.parseFloat(editExam_Duration.getText().toString()); // Get the duration of the exam

            if (finalIsUpdate) { // If we are updating an existing exam and the student ID hasnt been changed
                db.updateExam(bundle.getInt("id"), name, unit, date, time, location, duration); // Update the elements of the exam
            } else { // Else, if we are adding a new exam
                ExamModel exam = new ExamModel(); // Create a new exam
                // Set the values of the new exam
                exam.setStudentId(studentID);
                exam.setName(name);
                exam.setUnit(unit);
                exam.setDate(date);
                exam.setTime(time);
                exam.setLocation(location);
                exam.setDuration(duration);

                db.insertExam(exam); // Insert the exam to the database
            }
            dismiss(); // Dismiss the exam editor fragment (this)
        });

        // Listen for clicks on the text view for the date
        editExam_Date.setOnClickListener(new View.OnClickListener() { //! Comment
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(activity, android.R.style.Theme_Holo_Dialog_MinWidth, dateSetListener, year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() { //! Comment
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month += 1;
                String date = dayOfMonth + "/" + month + "/" + year;
                editExam_Date.setText(date);
            }
        };

        // Listen for clicks on the text view for the time
        editExam_Time.setOnClickListener(new View.OnClickListener() { // ! Comment
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY); //hour of day gets 24 h rather than hour which gets 12 h
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog dialog = new TimePickerDialog(activity, android.R.style.Theme_Holo_Dialog_MinWidth, timeSetListener, hour, minute, true);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        timeSetListener = new TimePickerDialog.OnTimeSetListener() { // ! Comment
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String time = String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute);
                editExam_Time.setText(time);
            }
        };
    }

    public boolean canSaveExam(TextView name, TextView unit, TextView date, TextView time, TextView location, TextView duration) {
        if (name.getText().toString().isEmpty() || unit.getText().toString().isEmpty() || /*date.getText().toString().endsWith("Date") || time.getText().toString().endsWith("Time") ||*/ location.getText().toString().isEmpty() || duration.getText().toString().isEmpty())
            return false;
        else
            return true;
    }

    // Allows us to refresh/update the recyclerview of our exams each time the panel is dismissed (when the exam is saved)
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        Activity activity = getActivity(); // Get a reference to the current activity
        if (activity instanceof DialogCloseListener) { // If this activity is an instance of our custom listener
            ((DialogCloseListener) activity).handleDialogClose(dialog); // Handle the close
        }
    }
}