package com.example.spamsnap;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ArrayList<Image> allimages;
    private Uri uri;
    private Context context;
    private static final int STORAGE_PERMISSION_CODE = 101;

    // Function to check and request permission
    public void checkPermission(String permission, int requestCode)
    {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        }
        else {
            //Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }
    // This function is called when user accept or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when user is prompt for permission.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {

            // Checking whether user granted the permission or not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Showing the toast message
                //Toast.makeText(MainActivity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
               Toast.makeText(MainActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // only for android 13 or above
            checkPermission("android.permission.READ_MEDIA_IMAGES",101);
        }else{
            checkPermission("android.permission.READ_EXTERNAL_STORAGE",102);
            checkPermission("android.permission.WRITE_EXTERNAL_STORAGE",102);
        }
        recyclerView=(RecyclerView) findViewById(R.id.image_recylerview);
        progressBar=(ProgressBar) findViewById(R.id.progressBar);
        allimages=new ArrayList<>();
        if (allimages.isEmpty()){
            //progressBar.setVisibility(View.VISIBLE);
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            //get all images from storage
            allimages=getAllImages();
            //set adapter to recylerview
//            recyclerView.setAdapter(new ImageAdapter(this,allimages ));
//            progressBar.setVisibility(View.GONE);
            if (recyclerView != null) {
                recyclerView.setAdapter(new ImageAdapter(this, allimages));
            }

            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    private ArrayList<Image> getAllImages() {
        ArrayList<Image> images = new ArrayList<Image>();
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String []projections = {MediaStore.Images.ImageColumns.DATA,MediaStore.Images.Media.DISPLAY_NAME};
        String orderby = MediaStore.Images.Media.DATE_TAKEN;
        String absolutepath,imagename;
        Cursor cursor = this.getContentResolver().query(uri,projections,null,null,orderby+"DESC");
        try {
            cursor.moveToFirst();
            do {
                absolutepath= cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                imagename= cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                Image image = new Image(absolutepath,imagename);
                images.add(image);
            }while (cursor.moveToNext());
        }catch (Exception e){
            e.printStackTrace();
        }
        return images;
    }

}