package com.broderickwestrope.whiteboard.gallery;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;

public class ImageGallery {
    public static ArrayList<String> imagesList(Context context) {
        Cursor cursor;
        int columnIndexData, columnIndexFolderName;
        ArrayList<String> imagesList = new ArrayList<String>();
        String absoluteImagePath;
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        String orderBy = MediaStore.Video.Media.DATE_TAKEN;
        cursor = context.getContentResolver().query(uri, projection, null, null, orderBy + " DESC");

        columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

        //Get Folder name
        columnIndexFolderName = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

        while (cursor.moveToNext()) {
            absoluteImagePath = cursor.getString(columnIndexData);
            imagesList.add(absoluteImagePath);
        }

        return imagesList;
    }
}
