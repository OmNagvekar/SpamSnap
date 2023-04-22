package com.example.spamsnap;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Menu menu ;
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
        ActionBar actionBar = getSupportActionBar();
       // actionBar.setDisplayShowTitleEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // only for android 13 or above
            checkPermission("android.permission.READ_MEDIA_IMAGES",101);
        }else{
            checkPermission("android.permission.READ_EXTERNAL_STORAGE",102);
            checkPermission("android.permission.WRITE_EXTERNAL_STORAGE",102);
        }
        recyclerView=(RecyclerView) findViewById(R.id.image_recylerview);
        progressBar=(ProgressBar) findViewById(R.id.progressBar);
        GridLayoutManager layoutManager = new GridLayoutManager(this,3);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        allimages=new ArrayList<>();
        if (allimages.isEmpty()){
            progressBar.setVisibility(View.VISIBLE);
            //get all images from storage
            allimages.clear();
            allimages=getAllImages();
            //set adapter to recylerview
            recyclerView.setAdapter(new ImageAdapter(this,allimages ));
            progressBar.setVisibility(View.GONE);
        }
    }

    private ArrayList<Image> getAllImages() {
        ArrayList<Image> images = new ArrayList<Image>();
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String []projections = {MediaStore.Images.ImageColumns.DATA,MediaStore.Images.Media.DISPLAY_NAME};
        String orderby = MediaStore.Images.Media.DATE_TAKEN;
        String absolutepath,imagename;
        Cursor cursor = MainActivity.this.getContentResolver().query(uri,projections,null,null,orderby+" DESC");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_button_menu,menu);
        this.menu=menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        MenuItem editItem = menu.findItem(R.id.edit);
        MenuItem selectImage = menu.findItem(R.id.text);
        MenuItem cancel = menu.findItem(R.id.cancel_button);
        MenuItem refresh = menu.findItem(R.id.refresh);
//        int edit=0;
//        Intent intent = new Intent(context,ImageAdapter.class);
        switch (item.getItemId()){
            case R.id.edit:
                editItem.setVisible(false);
                selectImage.setVisible(true);
                cancel.setVisible(true);
                refresh.setVisible(false);
//                intent.putExtra("delete",edit);
                return true;
            case R.id.cancel_button:
                editItem.setVisible(true);
                cancel.setVisible(false);
                selectImage.setVisible(false);
                refresh.setVisible(true);
                return true;
            case R.id.refresh:
                progressBar.setVisibility(View.VISIBLE);
                allimages.clear();
                allimages= getAllImages();
                recyclerView.setAdapter(new ImageAdapter(this,allimages));
                progressBar.setVisibility(View.GONE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}