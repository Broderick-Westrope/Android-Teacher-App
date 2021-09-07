package com.broderickwestrope.whiteboard.gallery;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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

import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    public static final int READ_PERMISSION_CODE = 101;
    RecyclerView gallery_ImagesRV;
    GalleryAdapter galleryAdapter;
    List<String> images;
    List<String> selectedImages;
    Toolbar toolbar;
    private GalleryAdapter.PhotoListener photoListener = new GalleryAdapter.PhotoListener() {
        @Override
        public void onPhotoClick(String path) {
            // Do something with the photos when clicked
            Snackbar.make(findViewById(R.id.content), "Pressed " + path, Snackbar.LENGTH_SHORT).show();
        }

        @Override
        public void onPhotoLongClick(String path) {
            // Do something with the photos when clicked
            Snackbar.make(findViewById(R.id.content), "Held " + path, Snackbar.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        // Set the support action bar to our custom action bar with the title "Records"
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
        if (item.getItemId() == android.R.id.home) { // When the back/home button (arrow) is pressed
            this.finish(); // Finish with the activity and return
            return true;
        }
        return super.onOptionsItemSelected(item);
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