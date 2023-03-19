package com.example.spamsnap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class ImageFullActivity extends AppCompatActivity {
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_full);
        imageView = (ImageView) findViewById(R.id.imageView);
        Intent intent = getIntent();
        String imagepath = intent.getStringExtra("path").toString();
        String imagename = intent.getStringExtra("name");
        getSupportActionBar().setTitle(imagename);
        Glide.with(this).load(imagepath).into(imageView);
    }
}