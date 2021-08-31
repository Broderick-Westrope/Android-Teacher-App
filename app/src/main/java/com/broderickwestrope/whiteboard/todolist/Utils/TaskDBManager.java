package com.broderickwestrope.whiteboard.todolist.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.broderickwestrope.whiteboard.todolist.Models.TaskModel;

import java.util.ArrayList;
import java.util.List;

// Our database manager for the tasks (of our to-do list). This is used to interface between the SQLite database easily
public class TaskDBManager extends SQLiteOpenHelper {
    private static final int VERSION = 1; // The version of our database
    private static final String NAME = "todoDB"; // The name we have given our database
    private static final String TODO_TABLE = "todo"; // The name of our table of tasks
    private static final String ID = "id"; // The column containing the index of the task
    private static final String TASK = "task"; // The column containing the text of the task
    private static final String LOCATION = "location"; // The column containing the location of the task
    private static final String STATUS = "status"; // The column containing the status of the task
    private static final String CREATE_TODO_TABLE = "CREATE TABLE " + TODO_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TASK + " TEXT, " + STATUS + " INTEGER, " + LOCATION + " TEXT)";

    // Our SQLite database
    private SQLiteDatabase db;

    // The class constructor
    public TaskDBManager(Context ctx) {
        super(ctx, NAME, null, VERSION);
    }

    // This takes place when we create the helper
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TODO_TABLE);
    }

    // This takes place every time we increment the VERSION value
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Drop (remove) the older tables
        db.execSQL("DROP TABLE IF EXISTS " + TODO_TABLE);
        //Reconstruct the tables
        onCreate(db);
    }

    // Initialises the database for writing on
    public void openDatabase() {
        // Allows us to write on our database
        db = this.getWritableDatabase();
    }

    // Inserts a given task into the database
    public void insertTask(TaskModel task) {
        // We use CV's to store our values
        ContentValues cv = new ContentValues();
        cv.put(LOCATION, task.getLocation()); // Add the location
        cv.put(TASK, task.getTask()); // Add the task name/description
        cv.put(STATUS, 0); // Add the default value of incomplete

        // Use the given insert function to avoid writing raw SQL. This inserts the values into the TODO_TABLE, ignoring no columns
        db.insert(TODO_TABLE, null, cv);
    }

    // Allows us to get a list of tasks (using the model) from the database
    public List<TaskModel> getAllTasks() {
        List<TaskModel> taskList = new ArrayList<>(); // Create en empty list
        Cursor cursor = null; // This cursor "points" to the result of our SQL query

        // Getting the data within a transaction prevents loss of data if the user exits during execution
        db.beginTransaction();
        try {
            // By setting all the values to null we select the entire database with no extra criteria (other than selecting them in descending order of ID and splitting based on completion)
            cursor = db.query(TODO_TABLE, null, null, null, null, null, STATUS + " ASC, " + ID + " ASC");
            if (cursor != null) // If the cursor is not empty then it means we successfully selected some data
            {
                if (cursor.moveToFirst()) // Move the cursor to the first row (from the last row). False if the cursor is empty
                {
                    do {  //This is where we turn our query into a list of tasks
                        TaskModel task = new TaskModel(); // Create a new task
                        task.setId(cursor.getInt(cursor.getColumnIndex(ID))); // Get the ID (the index of the column)
                        task.setTask(cursor.getString(cursor.getColumnIndex(TASK))); // Get the task name/description
                        task.setLocation(cursor.getString(cursor.getColumnIndex(LOCATION))); // Get the location
                        task.setStatus(cursor.getInt(cursor.getColumnIndex(STATUS))); // Get the status
                        taskList.add(task);
                    } while (cursor.moveToNext()); // Repeat for each remaining element
                }
            }
        } finally {
            // Once we have all the elements within the query, we want to end the transaction and close the cursor
            db.endTransaction();
            cursor.close();
        }
        return taskList;
    }

    // Update the status (completion) of the specified task (using that tasks ID)
    public void updateStatus(int id, int newStatus) {
        ContentValues cv = new ContentValues(); // We use CV's to store our values
        cv.put(STATUS, newStatus); // Add the updated status to the values
        db.update(TODO_TABLE, cv, ID + "=?", new String[]{String.valueOf(id)}); // Update the status of the specified ID
    }

    // Update the name/description of the specified task (using that tasks ID)
    public void updateTask(int id, String task, String location) {
        ContentValues cv = new ContentValues(); // We use CV's to store our values
        cv.put(TASK, task); // Add the task text to the values
        cv.put(LOCATION, location); // Add the location to the values
        db.update(TODO_TABLE, cv, ID + "=?", new String[]{String.valueOf(id)}); // Update the contents of the specified ID
    }

    // Delete the task of the specified ID
    public void deleteTask(int id) {
        db.delete(TODO_TABLE, ID + "=?", new String[]{String.valueOf(id)});
    }
}
