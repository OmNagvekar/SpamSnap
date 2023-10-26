package com.example.spamsnap;

import static com.example.spamsnap.MainActivity.allimages;
import static com.example.spamsnap.MainActivity.classifiedImages;
import static com.example.spamsnap.MainActivity.convertToBlackAndWhite;

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
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.spamsnap.ml.SpamsnapModelEnglish;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
            }
            else {
                Toast.makeText(SplashActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


//    private void requestDeletePermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
//            Uri uri = Uri.fromParts("package", getPackageName(), null);
//            intent.setData(uri);
//            startActivity(intent);
//        } else {
//            Toast.makeText(SplashActivity.this, "Cannot request delete permission on this device.", Toast.LENGTH_SHORT).show();
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressUpdater.getStatus()==AsyncTask.Status.RUNNING){
            progressUpdater.cancel(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            // only for android 11 and above
            if (Environment.isExternalStorageManager()){
                permissionflag=true;
            }else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 100);
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

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
                Log.d("imagecounter", "doInBackground: progress running,       percentage = "+(counter/size)*100+" %" );
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
//             ML Model Code
            // English Text Recognizer (optional)
            // TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            // Marathi (Devanagari) Text Recognizer (Required )
            TextRecognizer recognizer = TextRecognition.getClient(new DevanagariTextRecognizerOptions.Builder().build());

            InputImage image ;


            BitmapFactory.Options options = new BitmapFactory.Options();
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
                        Thread.sleep(1);
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
                        //Log.d("IMAGE0 ","Text in image full line: "+text.getText().toLowerCase()+" Image Name:"+img.imagename);//this gets all text from image
                        //for (Text.TextBlock tb: text.getTextBlocks()){
                        //Log.d("IMAGE", "Text in image full line: "+tb.getText().toLowerCase()+" Image Name:"+img.imagename); //this gets only some 2lines text block
                        //for (Text.Line l : tb.getLines()){
                        //Log.d("IMAGE2", "Text in image full line: "+l.getText().toLowerCase()+" Image Name:"+img.imagename);// this only gets one line at a time
                        try {
                            SpamsnapModelEnglish model = SpamsnapModelEnglish.newInstance(SplashActivity.this);

                            // Creates inputs for reference.
                            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 1203}, DataType.FLOAT32);
                            if (! Python.isStarted()) {
                                Python.start(new AndroidPlatform(SplashActivity.this));
                            }
                            Python py = Python.getInstance();
                            PyObject module =  py.getModule("TextProcessing");
                            if(text.getText().toLowerCase()!=null || !(text.getText().toLowerCase().equals("")) || !(text.getText().toLowerCase().equals(" "))){
                                PyObject englishText = module.get("english");
                                if (englishText != null ) {
                                    PyObject result = englishText.call(text.getText().toLowerCase());
                                    float[] floatData = result.toJava(float[].class);
                                    if(floatData.length==1203){
                                        for(int i= 0;i<1203 ;i++){
                                            inputFeature0.getFloatArray()[i]=floatData[i];
                                        }
                                    }
                                    SpamsnapModelEnglish.Outputs outputs = model.process(inputFeature0);
                                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
                                    Log.d("Result ML", "onSuccess: "+ outputFeature0+" filename: "+img.imagename);
                                    if (outputFeature0.getDataType() == DataType.FLOAT32) {
                                        //                                PyObject final_result_function = module.get("Result");
                                        float[] floatArray = outputFeature0.getFloatArray();
                                        int final_result = argmax(floatArray);
                                        Log.d("Result ML", "Result: "+ final_result+" spam filename: "+img.imagename);
                                        if (final_result==1) {
                                            //spam image
                                            classifiedImages.add(img);
                                            editor.putInt(img.imagepath,1);
                                            editor.apply();
                                            Log.d("ML MODEL", counter+" of "+size+" Text: "+text.getText().toLowerCase()+" spam filename: "+img.imagename);
                                        } else if (final_result==0){
                                            //ham image
                                            editor.putInt(img.imagepath,-1);
                                            editor.apply();
                                            Log.d("ML MODEL", counter+" of "+size+" Text: "+text.getText().toLowerCase()+" ham filename: "+img.imagename);
                                        }
                                    }
                                }

                            }else {
                                editor.putInt(img.imagepath,-1);
                                editor.apply();
                                Log.d("ML MODEL", counter+" of "+size+" Text: "+text.getText().toLowerCase()+" ham filename: "+img.imagename);
                            }

                            // Runs model inference and gets result.


                            // Releases model resources if no longer used.
                            model.close();
                        } catch (IOException e) {
                            // TODO Handle the exception
                            Log.d("Error","start here");
                            e.printStackTrace();
                            throw new RuntimeException(e);
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
            Log.d("Size", "total spam images count "+classifiedImages.size());

            
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
    public int argmax(float[] values) {
        int argmax1 = 0;
        float maxValue = values[0];

        for (int i = 1; i < values.length; i++) {
            if (values[i] > maxValue) {
                argmax1 = i;
                maxValue = values[i];
            }
        }
        return argmax1;
    }
}