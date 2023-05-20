package com.example.spamsnap;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class MainActivity extends AppCompatActivity{
    private RecyclerView recyclerView;
    private TextView textView;
    public static ArrayList<Image> classifiedImages = new ArrayList<Image>();;
    private AlertDialog alertDialog;
    private Menu menu ;
    public static ArrayList<Image> allimages = new ArrayList<Image>();
    private Uri uri;
    public  static boolean edit=false;
    public static boolean cancel1=false;
    private FloatingActionButton floatingActionButton;
    public static final int STORAGE_PERMISSION_CODE = 101;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayShowTitleEnabled(false);


        textView=findViewById(R.id.textView);
        recyclerView=(RecyclerView) findViewById(R.id.image_recylerview);
        GridLayoutManager layoutManager = new GridLayoutManager(this,3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        allimages=new ArrayList<>();

        if (classifiedImages.size()==0){
            textView.setVisibility(View.VISIBLE);
        }else {
            textView.setVisibility(View.INVISIBLE);
            //set adapter to recylerview
            recyclerView.setAdapter(new ImageAdapter(this,classifiedImages ));
        }


    }

    public ArrayList<Image> getAllImages() {
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
                else {
                    // Android lower than 11

                    floatingActionButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (ImageAdapter.deleteImages.isEmpty()){
                                Toast.makeText(MainActivity.this, "Select atleast one image", Toast.LENGTH_SHORT).show();
                            }else {
                                alert();
                            }
                        }
                    });
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
                ImageAdapter.deleteImages.clear();
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
        recyclerView.setAdapter(new ImageAdapter(this,classifiedImages));
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
                boolean deleted;

                // Android lower than 12 deletion code
                if (Build.VERSION.SDK_INT<Build.VERSION_CODES.R){
                    ContentResolver contentResolver = MainActivity.this.getContentResolver();
                    Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    String selection = MediaStore.Images.Media.DATA + "=?";
                    String[] selectionArgs;


                    for (Image image:ImageAdapter.deleteImages){
                        f = new File(image.imagepath);
                        deleted = f.delete();
                        if (deleted){
                            selectionArgs = new String[] { image.imagepath };
                            // deletes the path of deleted image from Media Store
                            contentResolver.delete(uri, selection, selectionArgs);

                            alertDialog.dismiss();
                            classifiedImages.remove(image);
                            SplashActivity.editor.remove(image.imagepath);
                            SplashActivity.editor.apply();
                            ((MainActivity)view.getContext()).recreate();


                        }else{
                            Log.d("Deletion (Android<12):", "Deletion  Failed");
                            return;
                        }

                    }
                    Toast.makeText(MainActivity.this,"Deleted",Toast.LENGTH_SHORT).show();
                    ImageAdapter.deleteImages.clear();

                }
                else{
                    for (Image image:ImageAdapter.deleteImages){
                        f =  new File(image.imagepath);
                        deleted = f.delete();
                        if (deleted){
                            classifiedImages.remove(image);
                            SplashActivity.editor.remove(image.imagepath);
                            SplashActivity.editor.apply();
                        }

                    }
                    Toast.makeText(MainActivity.this,"Deleted",Toast.LENGTH_SHORT).show();

                    alertDialog.dismiss();
                    ImageAdapter.deleteImages.clear();
                    ((MainActivity)view.getContext()).recreate();//recreate is used to recreate activity
                }

            }
        });
    }

    public static Bitmap convertToBlackAndWhite(Bitmap originalBitmap) {
        Bitmap blackAndWhiteBitmap = Bitmap.createBitmap(originalBitmap.getWidth(), originalBitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(blackAndWhiteBitmap);
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(originalBitmap, 0, 0, paint);
        return blackAndWhiteBitmap;
    }

}