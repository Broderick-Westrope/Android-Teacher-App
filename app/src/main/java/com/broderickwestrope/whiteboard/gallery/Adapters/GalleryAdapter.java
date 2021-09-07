package com.broderickwestrope.whiteboard.gallery.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.broderickwestrope.whiteboard.R;
import com.bumptech.glide.Glide;

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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final String image = images.get(position);

        Glide.with(context).load(image).into(holder.image);

        holder.itemView.setOnClickListener(v -> photoListener.onPhotoClick(image));

        holder.itemView.setOnLongClickListener(v -> {
            photoListener.onPhotoLongClick(image);
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public interface PhotoListener {
        void onPhotoClick(String path);

        void onPhotoLongClick(String path);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.image);
        }
    }
}
