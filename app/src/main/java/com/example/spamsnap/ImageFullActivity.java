package com.example.spamsnap;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ImageFullActivity extends AppCompatActivity {
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_full);
        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayShowTitleEnabled(false);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.orange)));
        imageView = (ImageView) findViewById(R.id.imageView);
        Intent intent = getIntent();
        String imagepath = intent.getStringExtra("path").toString();
        String imagename = intent.getStringExtra("name");
        getSupportActionBar().setTitle(imagename);
        Glide.with(this).load(imagepath).into(imageView);
    }
}