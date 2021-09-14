package com.broderickwestrope.whiteboard.Adapters;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.broderickwestrope.whiteboard.R;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    protected PhotoListener photoListener; // The photoListener to detect onclick actions
    private Context context; // The context that the adapter is being used in
    private List<String> images; // The list of images (stored as a list of paths to the images)


    //The class constructor
    public GalleryAdapter(Context context, List<String> images, PhotoListener photoListener) {
        this.photoListener = photoListener;
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.item_gallery, parent, false)
        );
    }

    // Delete all selected photos
    public void deleteSelected(ArrayList<String> selectedImages, Context context) {
        int size = selectedImages.size(), // The number of images that are selected
                failedCount = 0; // Stores the number of images that fail to delete (for user feedback)
        for (int i = 0; i < size; i++) { // For all of the images that are selected
            String item = selectedImages.get(i); // Get the image from the list
            images.remove(item); // Remove the image from the list

            File fDelete = new File(item); // A file for deleting from the device
            // Delete the image from the device and increase the number of fails if the deletion fails
            failedCount += (deleteImage(fDelete, context) ? 0 : 1);
        }
        // Tell the user if all deletions were successful, or tell them how many failed
        Snackbar.make(((Activity) context).findViewById(R.id.content), (failedCount == 0 ? "Delete Successful." : "Failed to delete " + String.valueOf(failedCount) + " items."), Snackbar.LENGTH_SHORT).show();
        notifyDataSetChanged(); // Refresh the recycler view
    }

    // Set each recycler view element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final String image = images.get(position); // Get the path of the image
        Glide.with(context).load(image).into(holder.image); // Use Glide to place the image in the recycler view's imageView element

        // When we use the checkbox
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) // When we uncheck the checkbox
                photoListener.onPhotoClick(image, holder); // Call the function to handle the click
        });
        // When the photo is clicked, call the function to handle the click
        holder.itemView.setOnClickListener(v -> photoListener.onPhotoClick(image, holder));
        // When the photo is long-clicked (ie. held)
        holder.itemView.setOnLongClickListener(v -> {
            // Call the function to handle the long-click
            photoListener.onPhotoLongClick(image, holder);
            return false;
        });
    }

    // Return the number of photos in the gallery
    @Override
    public int getItemCount() {
        return images.size();
    }

    // Delete the given image
    private boolean deleteImage(File file, Context context) {
        // Set up the projection (we only need the ID)
        String[] projection = {MediaStore.Images.Media._ID};

        // Create the arguments for selecting the image file
        String selection = MediaStore.Images.Media.DATA + " = ?";
        String[] selectionArgs = new String[]{file.getAbsolutePath()};

        // Get the media with the matching path from the MediaStore database
        Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI; // Create a query URI
        // Create a content resolver for access to the content model (ie. to delete the image from the device)
        ContentResolver contentResolver = context.getContentResolver();
        //Query the database to get a cursor to the image
        Cursor c = contentResolver.query(queryUri, projection, selection, selectionArgs, null);

        boolean success = false; // Default to failure
        if (c.moveToFirst()) { // If what we are looking for was in the database, then select it on the cursor
            // We found the ID. Deleting the item via the content provider will also remove the file
            long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
            // Get the URI for the data
            Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            // Using the content resolver, delete the data
            contentResolver.delete(deleteUri, null, null);
            // Mark that the operation was successful
            success = true;
        }
        c.close(); // Close the cursor because we have finished

        return success; // Return the success/failure
    }

    // The interface for onClick events
    public interface PhotoListener {
        void onPhotoClick(String path, ViewHolder holder);

        void onPhotoLongClick(String path, ViewHolder holder);
    }

    // The ViewHolder implementation containing references to all of the elements in the XML
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image; // The image itself
        public CheckBox checkBox; // The checkbox for selecting the image

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.image); // Get the image view
            checkBox = itemView.findViewById(R.id.imageCheckBox); // Get the checkbox
        }
    }
}
