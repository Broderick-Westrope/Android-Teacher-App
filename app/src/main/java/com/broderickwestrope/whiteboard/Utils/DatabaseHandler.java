package com.broderickwestrope.whiteboard.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.broderickwestrope.whiteboard.Models.TaskModel;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String NAME = "todoDatabase";
    private static final String TODO_TABLE = "todo";
    private static final String ID = "id";
    private static final String TASK = "task";
    private static final String LOCATION = "location";
    private static final String STATUS = "status";
    private static final String CREATE_TODO_TABLE = "CREATE TABLE " + TODO_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TASK + " TEXT, " + STATUS + " INTEGER, " + LOCATION + " TEXT)";

    private SQLiteDatabase db;

    public DatabaseHandler(Context ctx) {
        super(ctx, NAME, null, VERSION);
    }

    // This takes place every time a
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
        // We use CV so that we can later use the given insert function
        ContentValues cv = new ContentValues();
        cv.put(LOCATION, task.getLocation()); // Add the location
        cv.put(TASK, task.getTask()); // Add the task name/description
        cv.put(STATUS, 0); // Add the default value of incomplete



        // Use the given insert function to avoid writing raw SQL. This inserts cv into TODO_TABLE, ignoring no columns
        db.insert(TODO_TABLE, null, cv);
    }

    // Allows us to get a list of tasks (using TaskModel) from the database
    public List<TaskModel> getAllTasks() {
        List<TaskModel> taskList = new ArrayList<>();
        Cursor cursor = null; // This represents the result of our query

        // Getting the data within a transaction prevents loss of data if the user exits during execution
        db.beginTransaction();
        try {
            // By setting all the values to null we select the entire database with no extra criteria
            cursor = db.query(TODO_TABLE, null, null, null, null, null, null);
            if (cursor != null) // If the cursor is not empty (ie. we selected some data)
            {
                if (cursor.moveToFirst()) // Move the cursor to the first row (from the last row). False if the cursor is empty
                {
                    do {  //This is where we turn our query into a list of tasks
                        TaskModel task = new TaskModel();
                        task.setId(cursor.getInt(cursor.getColumnIndex(ID))); // Get the ID (the index of the column)
                        task.setTask(cursor.getString(cursor.getColumnIndex(TASK))); // Get the task name/description
                        task.setLocation(cursor.getString(cursor.getColumnIndex(LOCATION))); // Get the location
                        task.setStatus(cursor.getInt(cursor.getColumnIndex(STATUS))); // Get the status
                        taskList.add(task);
                    } while (cursor.moveToNext()); // Repeat for each remaining element
                }
            }
        } finally {
            db.endTransaction();
            cursor.close();
        }
        return taskList;
    }

    // Update the status (completition) of the specified task (using that tasks ID)
    public void updateStatus(int id, int newStatus) {
        ContentValues cv = new ContentValues(); // Use cv to allow for use of the database functions instead of raw SQL
        cv.put(STATUS, newStatus); // Add the new status to the values
        db.update(TODO_TABLE, cv, ID + "=?", new String[]{String.valueOf(id)}); // Update the status of the specified ID
    }

    // Update the name/description of the specified task (using that tasks ID)
    public void updateTask(int id, String task, String location) {
        ContentValues cv = new ContentValues(); // Use cv to allow for use of the database functions instead of raw SQL
        cv.put(TASK, task); // Add the status to the values
        cv.put(LOCATION, location); // Add the location to the values
        db.update(TODO_TABLE, cv, ID + "=?", new String[]{String.valueOf(id)}); // Update the status of the specified ID
    }

    public void deleteTask(int id) {
        db.delete(TODO_TABLE, ID + "=?", new String[]{String.valueOf(id)}); // Update the status of the specified ID
    }
}
