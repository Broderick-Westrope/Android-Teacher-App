package com.broderickwestrope.whiteboard.gallery;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.broderickwestrope.whiteboard.Adapters.GalleryAdapter;
import com.broderickwestrope.whiteboard.R;
import com.broderickwestrope.whiteboard.Utils.RecordDBManager;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Objects;

public class GalleryActivity extends AppCompatActivity {
    public static final int READ_PERMISSION_CODE = 101; // Our code for reading from the device
    RecyclerView gallery_ImagesRV; // The recyclerview for the images
    GalleryAdapter galleryAdapter; // Tha adapter for the images

    ArrayList<String> images; // The list of images in the gallery
    ArrayList<String> selectedImages; // The list of selected images

    Toolbar toolbar; // The toolbar at the top of the activity

    // The database for the student records (this is for assigning images to students from the gallery)
    RecordDBManager recordsDB;


    //Listen for click events on the photo
    private final GalleryAdapter.PhotoListener photoListener = new GalleryAdapter.PhotoListener() {
        // When the photo is clicked
        @Override
        public void onPhotoClick(String path, GalleryAdapter.ViewHolder holder) {
            // Respond to the click
            Snackbar.make(findViewById(R.id.content), "Pressed " + path, Snackbar.LENGTH_SHORT).show();

            if (selectedImages.remove(path)) // If the image was removed (meaning it was already selected)
                holder.checkBox.setVisibility(CheckBox.INVISIBLE); // Hide the checkbox
            else //Else, the image wasn't selected
                openImagePopup(path); // Set a student image from the gallery
        }

        // When the photo is long-clicked (ie. held)
        @Override
        public void onPhotoLongClick(String path, GalleryAdapter.ViewHolder holder) {
            // Respond to the long-click
            Snackbar.make(findViewById(R.id.content), "Held " + path, Snackbar.LENGTH_SHORT).show();

            if (!selectedImages.contains(path)) { // If the image isn't already selected
                selectedImages.add(path); // Select the image
                holder.checkBox.setVisibility(CheckBox.VISIBLE); // Make the checkbox visible
                holder.checkBox.setChecked(true); // Tick the checkbox
            }
        }
    };


    // When the activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        // Initialise the list of selected images
        selectedImages = new ArrayList<>();

        // Initialise the database manager and open it for use
        recordsDB = new RecordDBManager(this);
        recordsDB.openDatabase();

        // Set the support action bar to our custom action bar and dsplay it
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        gallery_ImagesRV = findViewById(R.id.gallery_ImagesRV);

        //If we don't have permission to read the device data
        if (ContextCompat.checkSelfPermission(GalleryActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Request permission to read the device data
            ActivityCompat.requestPermissions(GalleryActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERMISSION_CODE);
        } else { // Else, if we already have permission
            loadImages(); // Load the images into the gallery
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu. This adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.gallery_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteAllAction: // When the delete all button is pressed
                if (!selectedImages.isEmpty()) // Only allow the user to delete when there are tasks present
                    deleteSelection();
                return true;
            case android.R.id.home: // When the back/home button (arrow) is pressed
                this.finish(); // Finish with the activity and return
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Delete the images that are selected
    private void deleteSelection() {
        // Create a new alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(GalleryActivity.this);
        builder.setTitle("Delete Selected Photos"); // Set the title of the alert
        // Set the message of the alert
        builder.setMessage("Are you sure you want to delete the " + selectedImages.size() + " selected photo/s from your device?\nNOTE: This action cannot be undone."); // The content of the alert box
        // Set the positive button action
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            galleryAdapter.deleteSelected(selectedImages, GalleryActivity.this); // Delete all selected images
            selectedImages.clear(); // Clear the list of selected images
            toolbar.setTitle("Photos (" + images.size() + ")"); // Set the toolbar title to the new number of photos
        });

        // Set the negative button action
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {
        });
        AlertDialog dialog = builder.create(); // Build the alert
        dialog.show(); // Display the alert
    }

    // When we get a result from requesting permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // If it is a response to requesting permission to read the device storage
        if (requestCode == READ_PERMISSION_CODE)
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) { // If we were granted the permission
                // Display the result
                Toast.makeText(this, "Read external storage permission granted.", Toast.LENGTH_SHORT).show();
                // Load the images into the gallery
                loadImages();
            } else {// Else, if we were not granted the permission
                // Display the result
                Toast.makeText(this, "Read external storage permission denied.", Toast.LENGTH_SHORT).show();
            }
    }

    // Loads the images into the gallery
    private void loadImages() {
        gallery_ImagesRV.setHasFixedSize(true); // Fix the size of the view
        // Set it up to use a grid layout manager (giving us a grid with 4 columns)
        gallery_ImagesRV.setLayoutManager(new GridLayoutManager(this, 4));
        images = ImageGallery.imagesList(this); // Use the Image Gallery class to get the list of images
        galleryAdapter = new GalleryAdapter(this, images, photoListener); // Create a new adapter to handle the images

        gallery_ImagesRV.setAdapter(galleryAdapter); // Set the adapter in the recycler view
        toolbar.setTitle("Photos (" + images.size() + ")"); // Set the toolbar title to the number of photos now in the gallery
    }

    // Opens the popup for entering the student ID to assign them the pressed image
    private void openImagePopup(String path) {
        // Create a new alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(GalleryActivity.this);
        // Get a copy of the inflated XML layout
        final View popupLayout = getLayoutInflater().inflate(R.layout.enter_student_id_popup, null);
        builder.setView(popupLayout); // Set the popup to use the custom layout
        builder.setTitle("Assign Image to Student"); // Set the title of the popup
        //Set the positive button
        builder.setPositiveButton(android.R.string.ok, (dialog, which) ->
        {
            EditText editText = popupLayout.findViewById(R.id.enter_StudentID); // Get the edit text view
            String idString = String.valueOf(editText.getText()); // Set the string to the entered ID
            int studentID;
            if (!idString.isEmpty()) { // If the string isnt empty
                studentID = Integer.parseInt(idString); // Turn it into an integer
                setStudentImage(path, studentID); // Set the image of the specified student ID
            }
        });
        //Set the positive button
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
        });
        builder.create().show(); // Create and show the alert
    }

    // Sets the image at the path to that of the given student ID
    private void setStudentImage(String path, int id) {
        //Get the image bitmap from the path
        Bitmap bitmap = BitmapFactory.decodeFile(path);

        //Convert the bitmap to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); // Open a new conversion stream
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream); // Compress it into the stream
        byte[] byteArray = outputStream.toByteArray(); // Convert the data in the stream into a byte array

        if (recordsDB.updateImage(id, byteArray)) // If the image is successfully updated
            // Display the success
            Snackbar.make(findViewById(R.id.content), "Image updated successfully.", Snackbar.LENGTH_SHORT).show();
        else // Else, the student wasnt found in the database, so display the error
            Snackbar.make(findViewById(R.id.content), "Failed to update the image: The student does not exist.", Snackbar.LENGTH_SHORT).show();
    }
}