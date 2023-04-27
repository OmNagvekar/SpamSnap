package com.example.spamsnap;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private AlertDialog alertDialog;
    private Menu menu ;
    private ArrayList<Image> allimages;
    private Uri uri;
    public  static boolean edit=false;
    public static boolean cancel1=false;
    private Context context;
    private FloatingActionButton floatingActionButton;
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
                refresh();
                requestDeletePermission();
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
//        actionBar.setDisplayShowTitleEnabled(false);
//        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.orange)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // only for android 13 or above
            checkPermission("android.permission.READ_MEDIA_IMAGES",101);
            checkPermission("android.permission.MANAGE_EXTERNAL_STORAGE",102);
        }else{
            checkPermission("android.permission.READ_EXTERNAL_STORAGE",102);
            checkPermission("android.permission.WRITE_EXTERNAL_STORAGE",103);
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
//        MenuItem selectImage = menu.findItem(R.id.text);
        MenuItem cancel = menu.findItem(R.id.cancel_button);
        MenuItem refresh = menu.findItem(R.id.refresh);
        floatingActionButton = findViewById(R.id.floatingActionButton);
        Intent intent = getIntent();

        switch (item.getItemId()){
            case R.id.edit:
                editItem.setVisible(false);
                floatingActionButton.setVisibility(View.VISIBLE);
                cancel.setVisible(true);
                refresh.setVisible(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        floatingActionButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (ImageAdapter.deleteImages.isEmpty()){
                                    Toast.makeText(MainActivity.this, "Select atleast one image", Toast.LENGTH_SHORT).show();
                                }else {
                                    alert();
                                    editItem.setVisible(false);
                                    cancel.setVisible(true);
                                    refresh.setVisible(false);
                                }
                            }
                        });
                    }
                    else {
                        floatingActionButton.setVisibility(View.GONE);
                        editItem.setVisible(true);
                        cancel.setVisible(false);
                        refresh.setVisible(true);
                        requestDeletePermission();
                    }
                }
                edit=true;
                cancel1=false;
                return true;
            case R.id.cancel_button:
                editItem.setVisible(true);
                cancel.setVisible(false);
                floatingActionButton.setVisibility(View.INVISIBLE);
                refresh.setVisible(true);
                edit=false;
                cancel1=true;
                refresh();
                return true;
            case R.id.refresh:
                refresh();
                Toast.makeText(this, "Refreshing ....", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void refresh(){
        progressBar.setVisibility(View.VISIBLE);
        allimages.clear();
        allimages= getAllImages();
        recyclerView.setAdapter(new ImageAdapter(this,allimages));
        progressBar.setVisibility(View.GONE);
    }
    private void requestDeletePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Cannot request delete permission on this device.", Toast.LENGTH_SHORT).show();
        }
    }
    public void alert(){
        AlertDialog.Builder builderobj = new AlertDialog.Builder(this);
        View view= LayoutInflater.from(this).inflate(R.layout.delete,null);
        Button button6 =view.findViewById(R.id.button);
        Button  button2=view.findViewById(R.id.button2);
        builderobj.setView(view);
        builderobj.setCancelable(false);
        alertDialog =builderobj.create();
        alertDialog.show();
        //Cancel button
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        //Delete Button
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File f;
                for (String path:ImageAdapter.deleteImages){
                    f =  new File(path);
                    boolean deleted = f.delete();
                }
                refresh();
                floatingActionButton.setVisibility(View.GONE);
                edit =false;
                alertDialog.dismiss();
                ((MainActivity)view.getContext()).recreate();//recreate is used to recreate activity
            }
        });
    }

}