package com.example.spamsnap;

import static com.example.spamsnap.MainActivity.classifiedImages;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;

public class DeleteImagesService extends Service {
    public DeleteImagesService() {
        // Default constructor
    }
    private static final String TAG = "DeleteImagesService";
    private static final long INTERVAL = 24 * 60 * 60 * 1000; // 24 hours
    public  long CUTOFF = INTERVAL * SettingActivity.day; // 7 days

    Handler mHandler = new Handler();

    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            deleteOldImages();
            mHandler.postDelayed(this, INTERVAL);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//            long interval = intent.getLongExtra("interval",INTERVAL);
//            if (intent != null && intent.hasExtra("day")) {
//                day = intent.getIntExtra("day", 1);
//            }
//            CUTOFF = INTERVAL*day;
        Log.d(TAG, "onStartCommand: "+SettingActivity.day);
        mHandler.postDelayed(mRunnable, INTERVAL);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
    }

    private void deleteOldImages() {
        File dir;
        Log.d("Service", "deleteOldImages: "+SettingActivity.day);
        ArrayList<Image> ImageToRemove = new ArrayList<>();
        for (Image image:classifiedImages) {
            dir = new File(image.imagepath);
            long cutoff = System.currentTimeMillis() - CUTOFF;
            if (dir.lastModified() < cutoff) {
                boolean delete=dir.delete();
                Log.d("Delete","Deleted Image is:"+image.imagename+"and "+delete);
                if(delete==true){
                    ImageToRemove.add((image));
                }
            }
        }
        classifiedImages.removeAll(ImageToRemove);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}