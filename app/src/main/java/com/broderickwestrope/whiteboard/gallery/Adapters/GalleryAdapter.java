package com.broderickwestrope.whiteboard.gallery.Adapters;

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
import android.widget.CompoundButton;
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
    protected PhotoListener photoListener;
    private Context context;
    private List<String> images;


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

    // Used to delete all the selected photos
    public void deleteSelected(ArrayList<String> selectedImages, Context context) {
        int size = selectedImages.size(), failedCount = 0;
        for (int i = 0; i < size; i++) {
            String item = selectedImages.get(i); // Get the item from the list
            images.remove(item);

            File fdelete = new File(item);
            failedCount += (deleteImage(fdelete, context) ? 0 : 1);
        }
        Snackbar.make(((Activity) context).findViewById(R.id.content), (failedCount == 0 ? "Delete Successful." : "Failed to delete " + String.valueOf(failedCount) + " items."), Snackbar.LENGTH_SHORT).show();
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final String image = images.get(position);

        Glide.with(context).load(image).into(holder.image);

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked)
                photoListener.onPhotoClick(image, holder);
        });
        holder.itemView.setOnClickListener(v -> photoListener.onPhotoClick(image, holder));
        holder.itemView.setOnLongClickListener(v -> {
            photoListener.onPhotoLongClick(image, holder);
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    private boolean deleteImage(File file, Context context) {
        // Set up the projection (we only need the ID)
        String[] projection = {MediaStore.Images.Media._ID};

        // Match on the file path
        String selection = MediaStore.Images.Media.DATA + " = ?";
        String[] selectionArgs = new String[]{file.getAbsolutePath()};

        // Query for the ID of the media matching the file path
        Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();
        Cursor c = contentResolver.query(queryUri, projection, selection, selectionArgs, null);

        boolean success = false;
        if (c.moveToFirst()) {
            // We found the ID. Deleting the item via the content provider will also remove the file
            long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
            Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            contentResolver.delete(deleteUri, null, null);
            success = true;
        }
        c.close();

        return success;
    }

    public interface PhotoListener {
        void onPhotoClick(String path, ViewHolder holder);

        void onPhotoLongClick(String path, ViewHolder holder);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.image);
            checkBox = itemView.findViewById(R.id.imageCheckBox);
        }
    }

}
