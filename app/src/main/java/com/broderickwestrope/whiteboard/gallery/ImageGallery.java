package com.broderickwestrope.whiteboard.gallery;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;

// Class to get the images for the gallery
public class ImageGallery {
    public static ArrayList<String> imagesList(Context context) {
        Cursor cursor; // Stores the point in the (MediaStore) database that we are looking at
        int columnIndexData; // The column index of where the actual data is stored
        ArrayList<String> imagesList = new ArrayList<>(); // Stores the list of images
        String absoluteImagePath; // Stores the path of the image we are looking at
        // Stores the URI for accessing the MediaStore data on the device
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        // Query the database for the images, sorted by when they were taken
        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        cursor = context.getContentResolver().query(uri, projection, null, null, MediaStore.Video.Media.DATE_TAKEN + " DESC");

        // Get the column index of where the actual data is stored
        columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

        while (cursor.moveToNext()) { // While there are more images left to look at
            absoluteImagePath = cursor.getString(columnIndexData); // Get the path of the image
            imagesList.add(absoluteImagePath); // Add the path to the list
        }

        return imagesList; // Return the list of images (ie. image paths)
    }
}
