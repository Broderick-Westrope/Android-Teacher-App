package com.broderickwestrope.whiteboard.student_records.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.broderickwestrope.whiteboard.student_records.Models.RecordModel;

import java.util.ArrayList;
import java.util.List;

// Our database manager for the student records records. This is used to interface between the SQLite database easily.
public class RecordDBManager extends SQLiteOpenHelper {
    private static final int VERSION = 2; // The version of our database
    private static final String DB_NAME = "recordsDB"; // The name we have given our database
    private static final String RECORDS_TABLE = "records"; // The name of our table of records
    private static final String ID = "id"; // The column containing the student ID of the record
    private static final String NAME = "name"; // The column containing the name of the student
    private static final String GENDER = "gender"; // The column containing the gender of the student
    private static final String COURSE = "course"; // The column containing the course that the student is enrolled in
    private static final String AGE = "age"; // The column containing the age of the student
    private static final String ADDRESS = "address"; // The column containing the residential address of the student
    private static final String IMAGE = "image"; //The column containing the image of the student
    // The command for creating the SQLite table
    private static final String CREATE_RECORDS_TABLE = "CREATE TABLE " + RECORDS_TABLE + "(" + ID + " INTEGER, " + NAME + " TEXT, " + GENDER + " TEXT, " + COURSE + " TEXT, " + AGE + " INTEGER, " + ADDRESS + " TEXT, " + IMAGE + " BLOB" + ")";

    // Our SQLite database
    private SQLiteDatabase db;

    // The class constructor
    public RecordDBManager(Context ctx) {
        super(ctx, DB_NAME, null, VERSION);
    }

    // This takes place when we create the helper
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_RECORDS_TABLE);
    }

    // This takes place every time we increment the VERSION value
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Drop (remove) the older tables
        db.execSQL("DROP TABLE IF EXISTS " + RECORDS_TABLE);
        //Reconstruct the tables
        onCreate(db);
    }

    // Initialises the database for writing on
    public void openDatabase() {
        // Allows us to write on our database
        db = this.getWritableDatabase();
    }

    // Inserts a given record into the database
    public void insertRecord(RecordModel record) {
        // We use CV's to store our values
        ContentValues cv = new ContentValues();
        cv.put(ID, record.getId()); // Add the student ID
        cv.put(NAME, record.getName()); // Add the name
        cv.put(GENDER, record.getGender()); // Add the gender
        cv.put(COURSE, record.getCourse()); // Add the course
        cv.put(AGE, record.getAge()); // Add the age
        cv.put(ADDRESS, record.getAddress()); // Add the address
        cv.put(IMAGE, record.getImage()); // Add the image

        // Use the given insert function to avoid writing raw SQL. This inserts the values into the RECORDS_TABLE, ignoring no columns
        db.insert(RECORDS_TABLE, null, cv);
    }

    // Allows us to get a list of records (using the model) from the database
    public List<RecordModel> getAllRecords() {
        List<RecordModel> recordList = new ArrayList<>(); // Create en empty list
        Cursor cursor = null; // This cursor "points" to the result of our SQL query

        // Getting the data within a transaction prevents loss of data if the user exits during execution
        db.beginTransaction();
        try {
            // By setting all the values to null we select the entire database with no extra criteria (other than ordering them by ID)
            cursor = db.query(RECORDS_TABLE, null, null, null, null, null, ID + " ASC");
            if (cursor != null) // If the cursor is not empty then it means we successfully selected some data
            {
                if (cursor.moveToFirst()) // Move the cursor to the first row (from the last row). False if the cursor is empty
                {
                    do {  //This is where we turn our query into a list of records
                        RecordModel record = new RecordModel(); // Create a new record
                        record.setId(cursor.getInt(cursor.getColumnIndex(ID))); // Get the student ID
                        record.setName(cursor.getString(cursor.getColumnIndex(NAME))); // Get the name
                        record.setGender(cursor.getString(cursor.getColumnIndex(GENDER))); // Get the gender
                        record.setCourse(cursor.getString(cursor.getColumnIndex(COURSE))); // Get the course
                        record.setAge(cursor.getInt(cursor.getColumnIndex(AGE))); // Get the age
                        record.setAddress(cursor.getString(cursor.getColumnIndex(ADDRESS))); // Get the address
                        record.setImage(cursor.getBlob(cursor.getColumnIndex(IMAGE))); // Get the image
                        recordList.add(record); // Add the record to the list of records
                    } while (cursor.moveToNext()); // Repeat for each remaining element
                }
            }
        } finally {
            // Once we have all the elements within the query, we want to end the transaction and close the cursor
            db.endTransaction();
            cursor.close();
        }
        return recordList;
    }

    // Update the name/description of the specified record (using that records ID)
    public void updateRecord(int id, String name, String gender, String course, int age, String address) {
        ContentValues cv = new ContentValues(); // We use CV's to store our values
        cv.put(ID, id); // Add the student ID
        cv.put(NAME, name); // Add the name
        cv.put(GENDER, gender); // Add the gender
        cv.put(COURSE, course); // Add the course
        cv.put(AGE, age); // Add the age
        cv.put(ADDRESS, address); // Add the address
        db.update(RECORDS_TABLE, cv, ID + "=?", new String[]{String.valueOf(id)}); // Update the contents of the specified ID
    }

    // Delete the record of the specified ID
    public void deleteRecord(int id) {
        db.delete(RECORDS_TABLE, ID + "=?", new String[]{String.valueOf(id)});
    }
}
