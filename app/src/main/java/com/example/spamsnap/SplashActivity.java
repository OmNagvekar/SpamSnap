package com.example.spamsnap;

import static com.example.spamsnap.MainActivity.allimages;
import static com.example.spamsnap.MainActivity.convertToBlackAndWhite;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
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
//    private ProgressBar mProgressBar;
//    private TextView percent;
    float size;
    TextView percent;
    ProgressBar progressBar;
    static float percentage;

    private static final int STORAGE_PERMISSION_CODE = 101;
    float counter =0.0f;
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

        if (requestCode == STORAGE_PERMISSION_CODE) {

            // Checking whether user granted the permission or not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestDeletePermission();
                // Showing the toast message
                //Toast.makeText(MainActivity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(SplashActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Cannot request delete permission on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        percent = findViewById(R.id.percent);
        progressBar = findViewById(R.id.progress_bar);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            // only for android 12 or above
            checkPermission("android.permission.READ_MEDIA_IMAGES",101);
            checkPermission("android.permission.MANAGE_EXTERNAL_STORAGE",102);
        }else{
            checkPermission("android.permission.READ_EXTERNAL_STORAGE",102);
            checkPermission("android.permission.WRITE_EXTERNAL_STORAGE",103);
        }

        allimages = getAllImages();
        size = allimages.size();

//        mProgressBar = findViewById(R.id.progress_bar);
//        percent = findViewById(R.id.percent);

        new Thread(new Runnable() {
            @Override
            public void run() {
                new LoadDataTask().execute();
            }
        }).start();


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

     class LoadDataTask extends AsyncTask<Void, Integer, Void> {

//         @Override
//         protected void onPreExecute() {
//             super.onPreExecute();
//
//         }

         @Override
        protected Void doInBackground(Void... voids) {
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
            percentage =0.0f;
            String percentString = " %";
            int imgheight;
            float scale;
            int newWidth, newHeight;
            for (Image img:allimages){
                counter += 1;
                percentString = "";
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

                percentage = counter/size*100;
//                percentString += percentage ;
//                percentString += " %";
//                percent.setText("percentString");

                publishProgress((int)percentage);
            }
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return null;
        }

         @Override
         protected void onProgressUpdate(Integer... values) {
             super.onProgressUpdate(values);
             String percent_string =  values[0]+" %";
             percent.setText(percent_string);
             progressBar.setProgress(values[0]);
         }
         //        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
//            startActivity(intent);
//            finish();
//        }
    }
}