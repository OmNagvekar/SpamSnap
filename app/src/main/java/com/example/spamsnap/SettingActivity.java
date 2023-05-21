package com.example.spamsnap;

import static com.example.spamsnap.MainActivity.classifiedImages;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    public  static  boolean aswitch=false;
    public  static int index=0;
    static  int day =1;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
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
        SharedPreferences.Editor editor1;
        SharedPreferences sp1 = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        editor1=sp1.edit();
        aswitch=sp1.getBoolean("switch",false);
        if(aswitch==true){
            aSwitch.setChecked(aswitch);
            index=sp1.getInt("index",2);
            day=sp1.getInt("day",1);
            spinner.setVisibility(View.VISIBLE);
            spinner.setSelection(index);
            startDeleteImagesService();
            DeleteImagesService deleteImagesService =  new DeleteImagesService();
            deleteImagesService.mRunnable.run();
        }else {
            stopService(new Intent(getApplicationContext(), DeleteImagesService.class));
            DeleteImagesService deleteImagesService = new DeleteImagesService();
            deleteImagesService.mHandler.removeCallbacks(deleteImagesService.mRunnable);
            spinner.setVisibility(View.INVISIBLE);
        }
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    spinner.setVisibility(View.VISIBLE);
                    aswitch=true;
                    editor1.putBoolean("switch",aswitch);
                    editor1.apply();
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selectedOptions = parent.getItemAtPosition(position).toString();
                            if (selectedOptions.equals(array[0])){
                                index=0;
                                day=7;
                                editor1.putInt("index",index);
                                editor1.putInt("day",day);
                                editor1.apply();
                                startDeleteImagesService();
                                DeleteImagesService deleteImagesService =  new DeleteImagesService();
                                deleteImagesService.mRunnable.run();
                            } else if (selectedOptions.equals(array[1])) {
                                index=1;
                                day=30;
                                editor1.putInt("index",index);
                                editor1.putInt("day",day);
                                editor1.apply();

                                startDeleteImagesService();
                                DeleteImagesService deleteImagesService =  new DeleteImagesService();
                                deleteImagesService.mRunnable.run();
                            }else if (selectedOptions.equals(array[2])){
                                index=2;
                                day=1;
                                editor1.putInt("index",index);
                                editor1.putInt("day",day);
                                editor1.apply();

                                startDeleteImagesService();
                                DeleteImagesService deleteImagesService =  new DeleteImagesService();
                                deleteImagesService.mRunnable.run();
                            }
                            else {
                                index=2;
                                day=1;
                                editor1.putInt("index",index);
                                editor1.putInt("day",day);
                                editor1.apply();

                                startDeleteImagesService();
                                DeleteImagesService deleteImagesService =  new DeleteImagesService();
                                deleteImagesService.mRunnable.run();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
//                            index=sp1.getInt("index",2);
//                            spinner.setSelection(index);
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
                    aswitch=false;
                    editor1.putBoolean("switch", false);
                    editor1.apply();
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
