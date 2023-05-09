package com.example.spamsnap;

import static com.example.spamsnap.MainActivity.allimages;
import static com.example.spamsnap.MainActivity.classifiedImages;
import static com.example.spamsnap.MainActivity.convertToBlackAndWhite;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions;

import java.util.ArrayList;

public class SplashActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    protected ProgressUpdater progressUpdater;
    private TextView percentText;
    public static float size;
    public static float percentage;
    public static boolean permissionflag = false;
    public static boolean threadRunningflag = false;
    public static SharedPreferences.Editor editor;
    public static float counter =0.0f;
    public static SharedPreferences sp;
    public void checkPermission(String permission, int requestCode)
    {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(SplashActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(SplashActivity.this, new String[] { permission }, requestCode);
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

        if (requestCode == 101 ||requestCode == 102 ||requestCode == 103 ||requestCode == 104) {

            // Checking whether user granted the permission or not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT>Build.VERSION_CODES.Q){
                    permissionflag=true;
                    requestDeletePermission();

                }
                // Showing the toast message
                //Toast.makeText(MainActivity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(SplashActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    private void requestDeletePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        } else {
            Toast.makeText(SplashActivity.this, "Cannot request delete permission on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("imagecounter", "onDestroy: Progress "+progressUpdater.getStatus());
        if (progressUpdater.getStatus()==AsyncTask.Status.RUNNING){
            progressUpdater.cancel(true);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            // only for android 11 and above
            checkPermission("android.permission.READ_MEDIA_IMAGES",101);
            if (Environment.isExternalStorageManager()){
                permissionflag=true;
            }else {
                requestDeletePermission();
            }
        }else{
            checkPermission("android.permission.READ_EXTERNAL_STORAGE",103);
            checkPermission("android.permission.WRITE_EXTERNAL_STORAGE",104);
            if (ActivityCompat.checkSelfPermission(this,"android.permission.READ_EXTERNAL_STORAGE")==PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.checkSelfPermission(this,"android.permission.WRITE_EXTERNAL_STORAGE")==PackageManager.PERMISSION_GRANTED){
                    permissionflag = true;
                }else {
                    permissionflag=false;
                }
            }
        }
        if (permissionflag){
            allimages=getAllImages();
            size=allimages.size();
            progressBar=findViewById(R.id.progress_bar);
            sp = getSharedPreferences("AllImages", Context.MODE_PRIVATE);
            editor = sp.edit();

//        if  (!threadRunningflag) {
//            LoadDataTask loadDataTask = new LoadDataTask();
//            Log.w("imagecounter", "onCreate:       thread running  " + (threadRunningflag));
//            loadDataTask.execute();
//            threadRunningflag = true;
//        }
//            progressUpdater = new ProgressUpdater();
//            progressUpdater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }
        Log.d("imagecounter", "onResume: permission "+permissionflag+" , threadrunning "+threadRunningflag);
        if ((permissionflag) && !threadRunningflag){
            LoadDataTask loadDataTask = new LoadDataTask();
            Log.w("imagecounter", "onCreate:       thread running  " + (threadRunningflag));
            loadDataTask.execute();
            threadRunningflag = true;

        }
        progressUpdater = new ProgressUpdater();
        progressUpdater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
//            // only for android 11 and above
//            checkPermission("android.permission.READ_MEDIA_IMAGES",101);
//            if (ActivityCompat.checkSelfPermission(this,"android.permission.READ_MEDIA_IMAGES")==PackageManager.PERMISSION_GRANTED) {
//                permissionflag=true;
//            }
//            else {
//                    permissionflag=false;
//                }
//        }else{
//            checkPermission("android.permission.READ_EXTERNAL_STORAGE",103);
//            checkPermission("android.permission.WRITE_EXTERNAL_STORAGE",104);
//            if (ActivityCompat.checkSelfPermission(this,"android.permission.READ_EXTERNAL_STORAGE")==PackageManager.PERMISSION_GRANTED){
//                if (ActivityCompat.checkSelfPermission(this,"android.permission.WRITE_EXTERNAL_STORAGE")==PackageManager.PERMISSION_GRANTED){
//                    permissionflag = true;
//                }else {
//                    permissionflag=false;
//                }
//            }
//        }
//
//        Log.d("imagecounter", "onCreate: permission "+permissionflag);
//        if (permissionflag){
//            allimages=getAllImages();
//            size=allimages.size();
//            progressBar=findViewById(R.id.progress_bar);
//            sp = getSharedPreferences("AllImages", Context.MODE_PRIVATE);
//            editor = sp.edit();
//
////        if  (!threadRunningflag) {
////            LoadDataTask loadDataTask = new LoadDataTask();
////            Log.w("imagecounter", "onCreate:       thread running  " + (threadRunningflag));
////            loadDataTask.execute();
////            threadRunningflag = true;
////        }
////            progressUpdater = new ProgressUpdater();
////            progressUpdater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//
//        }


    }
    public ArrayList<Image> getAllImages() {
        ArrayList<Image> images = new ArrayList<Image>();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String []projections = {MediaStore.Images.ImageColumns.DATA,MediaStore.Images.Media.DISPLAY_NAME};
        String orderby = MediaStore.Images.Media.DATE_TAKEN;
        String absolutepath,imagename;
        Cursor cursor = (SplashActivity.this).getContentResolver().query(uri,projections,null,null,orderby+" DESC");
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


    public class ProgressUpdater extends AsyncTask<Void,Integer,Void>{

        @Override
        protected void onProgressUpdate(Integer... values) {
            percentText.setText(("Images "+((int)counter)+" of "+((int)size)+" ("+(int)percentage)+" %)");
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (counter<=size){
                progressBar = findViewById(R.id.progress_bar);
                percentText = findViewById(R.id.percent);
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
//                Log.d("imagecounter", "doInBackground: progress running,       percentage = "+(counter/size)*100+" %" );
                percentage = (counter/size)*100;
                publishProgress((int)percentage);
                progressBar.setProgress((int) percentage);

            }
            if (size!=0){
                finish();
            }

            return null;
        }
    }


     class LoadDataTask extends AsyncTask<Void, Integer, Void> {


         @Override
        protected Void doInBackground(Void... voids) {
             counter = 0.0f;
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

            classifiedImages.clear();

            for (Image img:allimages){
                counter += 1;
                // Optimization part
                if ((sp.getInt(img.imagepath,0)==1) || (sp.getInt(img.imagepath,0)==-1)){
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Log.i("imagecounter", "Optimizer : Already scanned, Skipping image  --->  "+img.imagename);
                    if (sp.getInt(img.imagepath,0)==1){
                        classifiedImages.add(img);
                    }
                    continue;
                }

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
                if (bitmap==null){
                    continue;
                }

                imgheight = bitmap.getHeight();
                imgWidth = bitmap.getWidth();
                if (imgheight<minHeightWidth || imgWidth<minHeightWidth){
                    scale = Math.max((float) minHeightWidth/imgWidth,(float) minHeightWidth/imgheight);
                    newWidth = Math.round(imgWidth * scale);
                    newHeight = Math.round(imgheight * scale);
                    bitmap = Bitmap.createScaledBitmap(bitmap,newWidth,newHeight,true);
                }

                image = InputImage.fromBitmap(convertToBlackAndWhite(bitmap),0);

                // passing all images to ML Model
                Task<Text> result = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text text) {
                        // task completed
                        if (text.getTextBlocks().isEmpty()){
                            editor.putInt(img.imagepath,-1);
                            editor.apply();
                            return;
                        }
                        for (Text.TextBlock tb: text.getTextBlocks()){
                            for (Text.Line l : tb.getLines()){
//                                Log.d("ML MODEL", counter+"of "+size+": Text ="+l.getText());
                                if (l.getText().toLowerCase().contains("morning")) {
                                    classifiedImages.add(img);
                                    editor.putInt(img.imagepath,1);
                                    editor.apply();
                                    Log.d("ML MODEL", counter+" of "+size+" Text: "+l.getText());
                                } else if (l.getText().toLowerCase().contains("good morning")){
                                    classifiedImages.add(img);
                                    editor.putInt(img.imagepath,1);
                                    editor.apply();
                                    Log.d("ML MODEL", counter+" of "+size+" Text: "+l.getText());
                                } else if (l.getText().contains("शुभ प्रभात")) {
                                    classifiedImages.add(img);
                                    editor.putInt(img.imagepath,1);
                                    editor.apply();
                                    Log.d("ML MODEL", counter+" of "+size+" Text: "+l.getText());
                                } else if (l.getText().contains("सकाळ")) {
                                    classifiedImages.add(img);
                                    editor.putInt(img.imagepath,1);
                                    editor.apply();
                                    Log.d("ML MODEL", counter+" of "+size+" Text: "+l.getText());
                                } else if (l.getText().contains("शुभ सकाळ")) {
                                    classifiedImages.add(img);
                                    editor.putInt(img.imagepath,1);
                                    editor.apply();
                                    Log.d("ML MODEL", counter+" of "+size+" Text: "+l.getText());
                                } else if (l.getText().contains("प्रभात")) {
                                    classifiedImages.add(img);
                                    editor.putInt(img.imagepath,1);
                                    editor.apply();
                                    Log.d("ML MODEL", counter+" of "+size+" Text: "+l.getText());
                                } else {
                                    editor.putInt(img.imagepath,-1);
                                    editor.apply();
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

                percentage = (counter/size)*100.0f;


            }

            if ((counter==size) && size!=0){
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            editor.apply();
            threadRunningflag = false;
             startActivity(intent);
             finish();
            }

            return null;
        }
    }
}