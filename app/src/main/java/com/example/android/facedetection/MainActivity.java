package com.example.android.facedetection;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import static android.R.attr.process;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ImageView mImageView;
    private TextView mTitleTextView;
    private Button mGoButton;
    private FloatingActionButton mSave;
    private FloatingActionButton mShare;
    private FloatingActionButton mClear;

    private String mTempPhotoPath;
    private Bitmap mResultsBitmap;

    //permission
    private static final int REQUEST_STORAGE_PERMISSION = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setting views

        mImageView = (ImageView) findViewById(R.id.image_view);
        mTitleTextView = (TextView) findViewById(R.id.emojifymeText);
        mGoButton = (Button) findViewById(R.id.goButton);
        mSave = (FloatingActionButton) findViewById(R.id.save);
        mShare = (FloatingActionButton) findViewById(R.id.share);
        mClear = (FloatingActionButton) findViewById(R.id.clear);
    }

    /**
     * on Click method for Go button. Launches camera
     * @param view
     */

    public void emogifyMe(View view) {

        // check for the external storage permission

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
            //if you have permission
            launchCamera();

        }else{
            //if you donot have permission,request it

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Called when you request permission for read and write external storage

        switch (requestCode){
            case REQUEST_STORAGE_PERMISSION:{
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // If you get the permission
                    launchCamera();
                }else{
                    // If you permission is denied

                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * create temporary image file and captures a pictures to store in it
     */

    private void launchCamera() {

        //create the capture image intent

        Intent TakePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent

        if (TakePicture.resolveActivity(getPackageManager()) != null) {
            // Create the temporary file where photo should go

            File photoFile = null;
            try{

                photoFile = BitmapUtils.createTempImageFile(this);

            }catch (IOException e){

                e.printStackTrace();
            }

            // continue only if photo file created

            if (photoFile != null){

                mTempPhotoPath = photoFile.getAbsolutePath();
                //Get the content URI for image file
                Uri photoURI = FileProvider.getUriForFile(this,"com.example.android.fileprovider",photoFile);

                //Add the uri so the camera can store the image
                TakePicture.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);

                //Launch the camera activity
                startActivityForResult(TakePicture,REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //If the image capture activity was called and was successful

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            // process the image and set it to the imageview
            processAndSetImage();
        }else{
            // delete the temporary image file
            BitmapUtils.deleteImageFile(this,mTempPhotoPath);
        }
    }

    /**
     * process and set the image in imageview
     */

    private void processAndSetImage() {

        //Toggle visibility of the views
        mTitleTextView.setVisibility(View.GONE);
        mGoButton.setVisibility(View.GONE);
        mSave.setVisibility(View.VISIBLE);
        mShare.setVisibility(View.VISIBLE);
        mClear.setVisibility(View.VISIBLE);

        //Resample the saved image to fit the imageview
        mResultsBitmap = BitmapUtils.resamplePic(this, mTempPhotoPath);

        //Detect the faces
        mResultsBitmap = Emojifier.detectFacesAndOverlayEmoji(this,mResultsBitmap);

        //set the new bitmap to th imageview
        mImageView.setImageBitmap(mResultsBitmap);
    }

    /**
     * saveMe action button
     * @param view
     */

    public void saveMe(View view) {

        // Delete the temporary image file
        BitmapUtils.deleteImageFile(this,mTempPhotoPath);

        //save the image
        BitmapUtils.saveImage(this,mResultsBitmap);

    }

    /**
     * shareMe action button
     * @param view
     */

    public void shareMe(View view) {
        // Delete the temporary image file
        BitmapUtils.deleteImageFile(this,mTempPhotoPath);

        //save the image
        BitmapUtils.saveImage(this,mResultsBitmap);

        //share the image
        BitmapUtils.shareImage(this,mTempPhotoPath);
    }

    public void clearImage(View view) {

        mImageView.setImageResource(0);
        mTitleTextView.setVisibility(View.VISIBLE);
        mGoButton.setVisibility(View.VISIBLE);
        mSave.setVisibility(View.GONE);
        mShare.setVisibility(View.GONE);
        mClear.setVisibility(View.GONE);

        // Delete the temporary image file
        BitmapUtils.deleteImageFile(this,mTempPhotoPath);
    }
}
