package com.allenbenny.sender;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ImageViewerActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageView imageView;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);


        mToolbar = (Toolbar) findViewById(R.id.image_viewer_page_toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Image Viewer");

        imageView = (ImageView) findViewById(R.id.image_viewer);
        imageUrl = getIntent().getStringExtra("url");
        Picasso.get().load(imageUrl).into(imageView);

    }
}
