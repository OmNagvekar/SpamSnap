package com.example.spamsnap;

import static com.example.spamsnap.MainActivity.classifiedImages;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;

import java.io.File;
import java.util.ArrayList;

public class SettingActivity extends AppCompatActivity {
    Switch aSwitch;
    CardView cardView;
    RadioButton sevenday,thirtyday;
    static  int day =1;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        aSwitch = findViewById(R.id.switch1);
        cardView = findViewById(R.id.cardView);
        sevenday = findViewById(R.id.sevenday);
        thirtyday = findViewById(R.id.thirtyday);
        cardView.setVisibility(View.GONE);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    cardView.setVisibility(View.VISIBLE);
//                    Intent intent = new Intent(SettingActivity.this, DeleteImagesService.class);
//                    long interval = 24 * 60 * 60 * 1000; // Default interval is 24 hours



                    if ((sevenday.isSelected())){
//                        interval = 7 * 24 * 60 * 60 * 1000; // 7 days;
                        day=7;
                        Log.d("Service", "onCheckedChanged: "+day);
                    } else if ((thirtyday.isSelected())) {
//                        interval = 30L * 24 * 60 * 60 * 1000; // 30 days
                        day=30;

                    }
                    startDeleteImagesService();
                    DeleteImagesService deleteImagesService =  new DeleteImagesService();
                    deleteImagesService.mRunnable.run();
//                    intent.putExtra("interval",interval);
//                    startService(intent);
                }
                else {
                    cardView.setVisibility(View.GONE);
                    // Stop the DeleteImagesService and remove the callback
                    stopService(new Intent(getApplicationContext(), DeleteImagesService.class));
                    DeleteImagesService deleteImagesService = new DeleteImagesService();
                    deleteImagesService.mHandler.removeCallbacks(deleteImagesService.mRunnable);
                }
            }
        });




    }
    private void startDeleteImagesService() {
        Intent intent = new Intent(SettingActivity.this, DeleteImagesService.class);
        intent.putExtra("day", day);
        startService(intent);
    }
    public class DeleteImagesService extends Service {
        public DeleteImagesService() {
            // Default constructor
        }
        private static final String TAG = "DeleteImagesService";
        private static final long INTERVAL = 24 * 60 * 60 * 1000; // 24 hours
        public  long CUTOFF = INTERVAL * day; // 7 days

        private Handler mHandler = new Handler();

        private Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                deleteOldImages();
                mHandler.postDelayed(this, INTERVAL);
            }
        };

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
//            long interval = intent.getLongExtra("interval",INTERVAL);
            if (intent != null && intent.hasExtra("day")) {
                day = intent.getIntExtra("day", 1);
            }
            CUTOFF = INTERVAL*day;
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
            Log.d("Service", "deleteOldImages: "+day);
            ArrayList<Image> ImageToRemove = new ArrayList<>();
            for (Image image:classifiedImages) {
                dir = new File(image.imagepath);
                long cutoff = System.currentTimeMillis() - CUTOFF;
                if (dir.lastModified() < cutoff) {
                    boolean delete=dir.delete();
                    Log.d("Delete","Deleted Image is:"+image.imagename+"and "+delete);
                    ImageToRemove.add((image));
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
}