package com.ec337.facescanpayment.core.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UriHelper {
    private static final String TAG = "UriHelper";
    private static final String FILE_PROVIDER_AUTHORITY = ".fileprovider";
    private static final String IMAGE_DIRECTORY = "scanned_images";
    public static List<Uri> saveBitmapListAsUris(Context context, List<Bitmap> bitmapList) {
        List<Uri> uriList = new ArrayList<>();
        File imageDir = createImageDirectory(context);

        if (imageDir == null) {
            Log.e(TAG, "Failed to create image directory.");
            return uriList; // Return empty list if directory creation fails
        }


        for (int i = 0; i < bitmapList.size(); i++) {
            Bitmap bitmap = bitmapList.get(i);
            String fileName = createImageFileName(i);
            File imageFile = new File(imageDir, fileName);

            try (FileOutputStream fos = new FileOutputStream(imageFile)) {  // Try-with-resources for automatic closing
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos); // Slightly reduced quality for smaller file size
                Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + FILE_PROVIDER_AUTHORITY, imageFile);
                uriList.add(uri);

            } catch (IOException e) {
                Log.e(TAG, "Error saving bitmap: " + e.getMessage(), e); // Proper logging for easier debugging
            }
        }

        return uriList;
    }

    public static File createImageDirectory(Context context) {
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null) {
            File imageDir = new File(storageDir, IMAGE_DIRECTORY);
            if (!imageDir.exists() && !imageDir.mkdirs()) {
                Log.e(TAG, "Failed to create directory: " + imageDir.getAbsolutePath());
                return null;
            }
            return imageDir;
        }
        return null;
    }



    public static String createImageFileName(int index) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return "scanned_image_" + index + "_" + timeStamp + ".jpg";
    }


    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        try {
            return android.provider.MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (IOException e) {
            Log.e(TAG, "Error getting bitmap from URI: " + e.getMessage(), e);
            return null; // Return null to indicate error
        }
    }
}
