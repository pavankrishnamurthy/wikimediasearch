package com.pa.eg.wikimediasearch;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class FullImageActivity extends AppCompatActivity {

    ImageView iv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);
        Intent intent = getIntent();
        String url = intent.getStringExtra("image");

        iv = (ImageView) findViewById(R.id.imageFullView);
        Picasso.with(getApplicationContext()).load(url).into(iv);
    }
}
