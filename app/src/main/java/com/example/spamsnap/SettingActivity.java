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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Spinner;
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
        Spinner spinner = findViewById(R.id.spinner);
        String [] array = {"7 Days","30 Days", "Default(24Hrs)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,R.layout.spinner_item,array);

        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);
        spinner.setVisibility(View.INVISIBLE);
//        cardView = findViewById(R.id.cardView);
//        sevenday = findViewById(R.id.sevenday);
//        thirtyday = findViewById(R.id.thirtyday);
//        cardView.setVisibility(View.GONE);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    spinner.setVisibility(View.VISIBLE);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selectedOptions = parent.getItemAtPosition(position).toString();
                            if (selectedOptions.equals(array[0])){
                                day=7;
                                startDeleteImagesService();
                                DeleteImagesService deleteImagesService =  new DeleteImagesService();
                                deleteImagesService.mRunnable.run();
                            } else if (selectedOptions.equals(array[1])) {
                                day=30;
                                startDeleteImagesService();
                                DeleteImagesService deleteImagesService =  new DeleteImagesService();
                                deleteImagesService.mRunnable.run();
                            }else if (selectedOptions.equals(array[2])){
                                day=1;
                                startDeleteImagesService();
                                DeleteImagesService deleteImagesService =  new DeleteImagesService();
                                deleteImagesService.mRunnable.run();
                            }
                            else {
                                day=1;
                                startDeleteImagesService();
                                DeleteImagesService deleteImagesService =  new DeleteImagesService();
                                deleteImagesService.mRunnable.run();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

//                    intent.putExtra("interval",interval);
//                    startService(intent);
                }
                else {
//                    cardView.setVisibility(View.GONE);
                    // Stop the DeleteImagesService and remove the callback
                    stopService(new Intent(getApplicationContext(), DeleteImagesService.class));
                    DeleteImagesService deleteImagesService = new DeleteImagesService();
                    deleteImagesService.mHandler.removeCallbacks(deleteImagesService.mRunnable);
                    spinner.setVisibility(View.INVISIBLE);
                }
            }
        });




    }
    private void startDeleteImagesService() {
        Intent intent = new Intent(SettingActivity.this, DeleteImagesService.class);
        intent.putExtra("day", day);
        startService(intent);
    }

}
