package com.pixie.pixie;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LaunchActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1, PERMISSION_REQUEST_STORAGE_ACCESS = 2, REQUEST_IMAGE_PICK_GALLERY = 3;
    private String mCurrentImagePath;
    private Icon iconClicked;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView cameraButton = (ImageView) findViewById(R.id.camera_icon);
        ImageView galleryButton = (ImageView) findViewById(R.id.gallery_icon);

        //Camera icon listener.
        cameraButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //If storage permission is already granted.
                        iconClicked = Icon.CAMERA_ICON;
                        if (checkStoragePermission()) {
                            dispatchTakePictureIntent();
                        }
                    }
                }
        );

        //Gallery icon listener.
        galleryButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //If storage permission is already granted.
                        iconClicked = Icon.GALLERY_ICON;
                        if (checkStoragePermission()) {
                            imagePickerGalleryIntent();
                        }
                    }
                }
        );
    }

    /**
     * To fire camera intent.
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Ensure that there's a camera activity to handle the intent.
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            //Start camera intent.
            File imageFile = createImageFile();
            if (imageFile != null) {
                imageUri = Uri.fromFile(imageFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                galleryAddImg();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to save your photo !", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * To create image filepath.
     */
    private File createImageFile() {
        //Create an image file name.
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PIXIE" + timeStamp + ".jpg";
        //Create file.
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + File.separator + "Pixie");
        //To make sure the filepath exists.
        path.mkdirs();
        File imageFile = new File(path, imageFileName);
        //Get absolute path.
        mCurrentImagePath = imageFile.getPath();
        return imageFile;
    }

    /**
     * Handle permissions result.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            //On permission allowed, fire camera intent.
            case PERMISSION_REQUEST_STORAGE_ACCESS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (iconClicked == Icon.CAMERA_ICON) {
                        dispatchTakePictureIntent();
                    } else if (iconClicked == Icon.GALLERY_ICON) {
                        imagePickerGalleryIntent();
                    }
                }
            }
        }
    }

    /**
     * Check storage permissions.
     */
    private boolean checkStoragePermission() {
        //Check storage access permission.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //Show explanation if user denied permission previously.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Pixie needs access to storage for saving your photo !", Toast.LENGTH_SHORT).show();
            }
            //Request permission to access storage.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE_ACCESS);
            return false;
        } else {
            //Permission already granted.
            return true;
        }
    }

    /**
     * Add image to gallery.
     */
    private void galleryAddImg() {
        MediaScannerConnection.scanFile(this, new String[]{mCurrentImagePath}, new String[]{"images/jpeg"}, null);
    }

    /**
     * To fire gallery intent.
     */
    private void imagePickerGalleryIntent() {
        Intent imagePickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        imagePickerIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(imagePickerIntent, "Choose photo"), REQUEST_IMAGE_PICK_GALLERY);
    }

    /**
     * To start next activity after image is chosen.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //If image uri is available.
        if (resultCode == RESULT_OK) {
            Intent i = new Intent(getApplicationContext(), EditActivity.class);
            if (data != null) {
                imageUri = data.getData();
            }
            i.putExtra("imageURI", imageUri.toString());
            startActivity(i);
        }
    }

    private enum Icon {
        CAMERA_ICON, GALLERY_ICON, COLLAGE_ICON
    }
}