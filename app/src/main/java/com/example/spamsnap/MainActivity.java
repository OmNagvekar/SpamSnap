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
    private ProgressBar progressBar;
    private AlertDialog alertDialog;
    private Menu menu ;
    public static ArrayList<Image> allimages;
    private Uri uri;
    public  static boolean edit=false;
    public static boolean cancel1=false;
    private FloatingActionButton floatingActionButton;
    private static final int STORAGE_PERMISSION_CODE = 101;
    int size;
    MLthread obj = new MLthread();


    // Function to check and request permission
    public void checkPermission(String permission, int requestCode)
    {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
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
    protected void onPause() {

        Log.d("imagecounter", "onPause: : Thread paused.");
        try {
            // thread goes into waiting state
            obj.wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("imagecounter", "onResume: : Thread created/resumed");
        if (obj.isAlive()){
            // resumes existing thread
            obj.notify();
        }else {
            // starts new thread
            obj.setPriority(Thread.MAX_PRIORITY);obj.setDaemon(true);
            obj.start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayShowTitleEnabled(false);


        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            // only for android 12 or above
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

        progressBar.setVisibility(View.VISIBLE);

        //get all images from storage
        allimages.clear();
        allimages=getAllImages();
        size = allimages.size();

        //set adapter to recylerview
        recyclerView.setAdapter(new ImageAdapter(this,allimages ));
        progressBar.setVisibility(View.GONE);


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
                boolean deleted;

                // Android lower than 12 deletion code
                if (Build.VERSION.SDK_INT<=Build.VERSION_CODES.R){
                    ContentResolver contentResolver = MainActivity.this.getContentResolver();
                    Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    String selection = MediaStore.Images.Media.DATA + "=?";
                    String[] selectionArgs;


                    for (String path:ImageAdapter.deleteImages){
                        f = new File(path);
                        deleted = f.delete();
                        if (deleted){
                            selectionArgs = new String[] { path };
                            // deletes the path of deleted image from Media Store
                            contentResolver.delete(uri, selection, selectionArgs);

                            alertDialog.dismiss();

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
                    for (String path:ImageAdapter.deleteImages){
                        f =  new File(path);
                        deleted = f.delete();

                    }
                    Toast.makeText(MainActivity.this,"Deleted",Toast.LENGTH_SHORT).show();

                    alertDialog.dismiss();
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
    class MLthread extends Thread{

        int counter= 0;
        @Override
        public void run() {
            // ML Model testing area
            // English Text Recognizer (optional)
            // TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            // Marathi (Devanagari) Text Recognizer (Required )
            TextRecognizer recognizer = TextRecognition.getClient(new DevanagariTextRecognizerOptions.Builder().build());

            InputImage image ;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize=2;
            int minHeightWidth = 32;
            int imgWidth;
            int imgheight;
            float scale;
            int newWidth, newHeight;
            for (Image img:allimages){
                counter +=1;
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                // Loads all images
                Log.d("imagecounter", counter+" of "+size+": Image name  -->  "+img.imagename);
               Bitmap bitmap = BitmapFactory.decodeFile(img.imagepath,options);

                // making sure that every image is atleast 32X32 pixels to avoid error from ML Model
                // -------------- (Scaling the Input image if required) ----------
                imgheight = bitmap.getHeight();
                imgWidth = bitmap.getWidth();
                if (imgheight<minHeightWidth || imgWidth<minHeightWidth){
                    scale = Math.max((float) minHeightWidth/imgWidth,(float) minHeightWidth/imgheight);
                    newWidth = Math.round(imgWidth * scale);
                    newHeight = Math.round(imgheight * scale);
                    bitmap = Bitmap.createScaledBitmap(bitmap,newWidth,newHeight,true);
                }
//                bitmap = convertToBlackAndWhite(bitmap);
                image = InputImage.fromBitmap(convertToBlackAndWhite(bitmap),0);

                // passing all images to ML Model
                Task<Text> result = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text text) {
                        // task completed
                        if (text.getTextBlocks().isEmpty()){
//                            Log.d("ML MODEL", counter+"of "+size+": NO text found");
                            return;
                        }
                        for (Text.TextBlock tb: text.getTextBlocks()){
                            for (Text.Line l : tb.getLines()){
//                                Log.d("ML MODEL", counter+"of "+size+": Text ="+l.getText());
                                if (l.getText().toLowerCase().contains("morning")) {
                                    Log.d("ML MODEL", counter+" of "+size+" Text: "+l.getText());
                                } else if (l.getText().toLowerCase().contains("good morning")){
                                    Log.d("ML MODEL", counter+" of "+size+" Text: "+l.getText());
                                } else if (l.getText().contains("शुभ प्रभात")) {
                                    Log.d("ML MODEL", counter+" of "+size+" Text: "+l.getText());
                                } else if (l.getText().contains("शुभ")) {
                                    Log.d("ML MODEL", counter+" of "+size+" Text: "+l.getText());
                                } else if (l.getText().contains("सकाळ")) {
                                    Log.d("ML MODEL", counter+" of "+size+" Text: "+l.getText());
                                } else if (l.getText().contains("शुभ सकाळ")) {
                                    Log.d("ML MODEL", counter+" of "+size+" Text: "+l.getText());
                                } else if (l.getText().contains("प्रभात")) {
                                    Log.d("ML MODEL", counter+" of "+size+" Text: "+l.getText());
                                } else {
//                                    Log.d("ML MODEL", counter+"of "+size+"onSuccess: None of the category");
                                }
                            }
                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with exception

                        Log.e("ML MODEL", "onFailure: "+e);
                    }
                });
            }
        }
    }

}