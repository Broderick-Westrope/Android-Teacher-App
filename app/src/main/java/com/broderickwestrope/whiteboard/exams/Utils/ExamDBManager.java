package com.broderickwestrope.whiteboard.exams.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.broderickwestrope.whiteboard.exams.Models.ExamModel;

import java.util.ArrayList;
import java.util.List;

// Our database manager for the student exams exams. This is used to interface between the SQLite database easily.
public class ExamDBManager extends SQLiteOpenHelper {
    private static final int VERSION = 1; // The version of our database
    private static final String DB_NAME = "examsDB"; // The name we have given our database
    private static final String EXAMS_TABLE = "exams"; // The name of our table of exams
    private static final String ID = "id"; // The column containing the auto-assigned ID for this exam
    private static final String NAME = "name"; // The column containing the name of the exam
    private static final String UNIT = "unit"; // The column containing the unit that the exam is for
    private static final String DATE = "date"; // The column containing the date of the exam
    private static final String TIME = "time"; // The column containing the time of the exam
    private static final String LOCATION = "location"; // The column containing the location of the exam
    private static final String DURATION = "duration"; // The column containing the duration of the exam
    // The command for creating the SQLite table
    private static final String CREATE_EXAMS_TABLE = "CREATE TABLE " + EXAMS_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME + " TEXT, " + UNIT + " TEXT, " + DATE + " TEXT, " + TIME + " INTEGER, " + LOCATION + " TEXT, " + DURATION + " INTEGER" + ")";

    // Our SQLite database
    private SQLiteDatabase db;

    // The class constructor
    public ExamDBManager(Context ctx) {
        super(ctx, DB_NAME, null, VERSION);
    }

    // This takes place when we create the helper
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_EXAMS_TABLE);
    }

    // This takes place every time we increment the VERSION value
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Drop (remove) the older tables
        db.execSQL("DROP TABLE IF EXISTS " + EXAMS_TABLE);
        //Reconstruct the tables
        onCreate(db);
    }

    // Initialises the database for writing on
    public void openDatabase() {
        // Allows us to write on our database
        db = this.getWritableDatabase();
    }

    // Inserts a given exam into the database
    public void insertExam(ExamModel exam) {
        // We use CV's to store our values
        ContentValues cv = new ContentValues();
        cv.put(ID, exam.getId()); // Add the ID
        cv.put(NAME, exam.getName()); // Add the name
        cv.put(UNIT, exam.getUnit()); // Add the unit
        cv.put(DATE, exam.getDate()); // Add the date
        cv.put(TIME, exam.getTime()); // Add the time
        cv.put(LOCATION, exam.getLocation()); // Add the location
        cv.put(DURATION, exam.getDuration()); // Add the duration

        // Use the given insert function to avoid writing raw SQL. This inserts the values into the EXAMS_TABLE, ignoring no columns
        db.insert(EXAMS_TABLE, null, cv);
    }

    // Allows us to get a list of exams (using the model) from the database
    public List<ExamModel> getAllExams() {
        List<ExamModel> examList = new ArrayList<>(); // Create en empty list
        Cursor cursor = null; // This cursor "points" to the result of our SQL query

        // Getting the data within a transaction prevents loss of data if the user exits during execution
        db.beginTransaction();
        try {
            // By setting all the values to null we select the entire database with no extra criteria (other than ordering them by ID)
            cursor = db.query(EXAMS_TABLE, null, null, null, null, null, ID + " DESC");
            if (cursor != null) // If the cursor is not empty then it means we successfully selected some data
            {
                if (cursor.moveToFirst()) // Move the cursor to the first row (from the last row). False if the cursor is empty
                {
                    do {  //This is where we turn our query into a list of exams
                        ExamModel exam = new ExamModel(); // Create a new exam
                        exam.setId(cursor.getInt(cursor.getColumnIndex(ID))); // Get the ID
                        exam.setName(cursor.getString(cursor.getColumnIndex(NAME))); // Get the name
                        exam.setUnit(cursor.getString(cursor.getColumnIndex(NAME))); // Get the unit
                        exam.setDate(cursor.getString(cursor.getColumnIndex(DATE))); // Get the gender
                        exam.setTime(cursor.getString(cursor.getColumnIndex(TIME))); // Get the time
                        exam.setLocation(cursor.getString(cursor.getColumnIndex(LOCATION))); // Get the location
                        exam.setDuration(cursor.getInt(cursor.getColumnIndex(DURATION))); // Get the duration
                        examList.add(exam);
                    } while (cursor.moveToNext()); // Repeat for each remaining element
                }
            }
        } finally {
            // Once we have all the elements within the query, we want to end the transaction and close the cursor
            db.endTransaction();
            cursor.close();
        }
        return examList;
    }

    // Update the name/description of the specified exam (using that exams ID)
    public void updateExam(int id, String name, String unit, String date, String time, String location, int duration) {
        ContentValues cv = new ContentValues(); // We use CV's to store our values
        cv.put(ID, id); // Add the <ID>
        cv.put(NAME, name); // Add the name
        cv.put(UNIT, unit); // Add the gender
        cv.put(DATE, date); // Add the course
        cv.put(TIME, time); // Add the age
        cv.put(LOCATION, location); // Add the address
        cv.put(DURATION, duration); // Add the address
        db.update(EXAMS_TABLE, cv, ID + "=?", new String[]{String.valueOf(id)}); // Update the contents of the specified ID
    }

    // Delete the exam of the specified ID
    public void deleteExam(int id) {
        db.delete(EXAMS_TABLE, ID + "=?", new String[]{String.valueOf(id)});
    }
}
