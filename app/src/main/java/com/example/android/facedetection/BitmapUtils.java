package com.example.android.facedetection;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BitmapUtils {

    private static final String TAG = "BitmapUtils";

    /**
     * Creates a temporary file in the cache directory
     * @param context
     * @return
     * @throws IOException
     */

    static File createTempImageFile(Context context) throws IOException {

        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "FD_JPEG_"+ timeStamp +"_";
        File storageDirectory = context.getExternalCacheDir();

        return File.createTempFile(imageFileName,".jpg",storageDirectory);
    }

    /**
     * Deletes the image file path
     * @param context
     * @param imagePath
     * @return
     */

    static boolean deleteImageFile(Context context, String imagePath){

        //Get the file\
        File imageFile = new File(imagePath);

        //Delete the image
        boolean deleted = imageFile.delete();
        //If error in deleting the file
        if (!deleted){
            Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
        }
        return deleted;
    }

    static Bitmap resamplePic(Context context,String imagePath){

        //Get device screen size information

        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);
        int targetH = metrics.heightPixels;
        int targetW = metrics.widthPixels;

        //Get the dimensions of the original bitmap

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath,bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        //Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        //Decode the image file into a bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeFile(imagePath);
    }

    /**
     * Method for saving the image
     * @param context
     * @param image
     * @return
     */


    static String saveImage(Context context, Bitmap image){

        String savedImagePath = null;

        //Create the new file in the external storage
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "FD_JPEG_"+ timeStamp +".jpg";
        File storageDirectory = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    + "/Emojify");

        boolean success = true;
        if (!storageDirectory.exists()){
            success = storageDirectory.mkdir();
        }

        //save the new Bitmap
        if (success){
            File imageFile= new File(storageDirectory,imageFileName);
            savedImagePath = imageFile.getAbsolutePath();

            try {
                OutputStream fout = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG,100,fout);
                fout.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Add the image file to the system gallery
            galleryAddPic(context,savedImagePath);

            //Show a Toast with a save location
            String savedMessage= context.getString(R.string.saved_message,savedImagePath);
            Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show();
        }
        return savedImagePath;
    }

    /**
     * Method for adding the photo to the system gallery so it can be accessed from other apps
     * @param context
     * @param imagePath
     */

    private static void galleryAddPic(Context context, String imagePath){

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(imagePath);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    /**
     * Method for sharing an image
     * @param context
     * @param imagePath
     */

    static void shareImage(Context context,String imagePath){

        //Create the share intent and start the share activity
        File imageFile = new File(imagePath);
        Intent shareIntent= new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        Uri photoURI = FileProvider.getUriForFile(context,"com.example.android.fileprovider",imageFile);
        shareIntent.putExtra(Intent.EXTRA_STREAM,photoURI);
        context.startActivity(shareIntent);

    }
}



























