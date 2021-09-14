package com.broderickwestrope.whiteboard.gallery;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.broderickwestrope.whiteboard.R;
import com.broderickwestrope.whiteboard.gallery.Adapters.GalleryAdapter;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {
    public static final int READ_PERMISSION_CODE = 101;
    RecyclerView gallery_ImagesRV;
    GalleryAdapter galleryAdapter;
    ArrayList<String> images;
    ArrayList<String> selectedImages;
    Toolbar toolbar;
    private GalleryAdapter.PhotoListener photoListener = new GalleryAdapter.PhotoListener() {
        @Override
        public void onPhotoClick(String path, GalleryAdapter.ViewHolder holder) {
            // Do something with the photos when clicked
            Snackbar.make(findViewById(R.id.content), "Pressed " + path, Snackbar.LENGTH_SHORT).show();
            selectedImages.remove(path);
            holder.checkBox.setVisibility(CheckBox.INVISIBLE);
//            holder.checkBox.setChecked(false);
        }

        @Override
        public void onPhotoLongClick(String path, GalleryAdapter.ViewHolder holder) {
            // Do something with the photos when clicked
            Snackbar.make(findViewById(R.id.content), "Held " + path, Snackbar.LENGTH_SHORT).show();
            if (!selectedImages.contains(path))
                selectedImages.add(path);
            holder.checkBox.setVisibility(CheckBox.VISIBLE);
            holder.checkBox.setChecked(true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        selectedImages = new ArrayList<>();

        // Set the support action bar to our custom action bar with the title "Photos"
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Photos");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        gallery_ImagesRV = findViewById(R.id.gallery_ImagesRV);

        //Check for permissions
        if (ContextCompat.checkSelfPermission(GalleryActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(GalleryActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERMISSION_CODE);
        } else {
            loadImages();
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

    private void deleteSelection() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GalleryActivity.this);
        builder.setTitle("Delete Selected Photos"); // The title of the alert box
        builder.setMessage("Are you sure you want to delete the " + String.valueOf(selectedImages.size()) + " selected photo/s from your device?\nNOTE: This action cannot be undone."); // The content of the alert box
        // The positive button action
        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            galleryAdapter.deleteSelected(selectedImages, GalleryActivity.this); // Delete all tasks
            selectedImages.clear();
            toolbar.setTitle("Photos (" + images.size() + ")");
        });

        // The negative button action
        builder.setNegativeButton(android.R.string.no, (dialog, which) -> {
        });
        AlertDialog dialog = builder.create(); // Build the alert
        dialog.show(); // Display the alert
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == READ_PERMISSION_CODE)
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read external storage permission granted.", Toast.LENGTH_SHORT).show();
                loadImages();
            } else {
                Toast.makeText(this, "Read external storage permission denied.", Toast.LENGTH_SHORT).show();
            }
    }

    private void loadImages() {
        gallery_ImagesRV.setHasFixedSize(true);
        gallery_ImagesRV.setLayoutManager(new GridLayoutManager(this, 4));
        images = ImageGallery.imagesList(this);
        galleryAdapter = new GalleryAdapter(this, images, photoListener);

        gallery_ImagesRV.setAdapter(galleryAdapter);
        toolbar.setTitle("Photos (" + images.size() + ")");
    }
}