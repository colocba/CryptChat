package com.example.amirbaum.cryptchat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class FullSizeImageActivity extends AppCompatActivity {

    public ImageView mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_size_image);

        mImage = (ImageView) findViewById(R.id.large_image_view);
        final Animation zoomAnimation = AnimationUtils.loadAnimation(this, R.anim.zoom);

        byte[] imageInByte = getIntent().getByteArrayExtra("image");
        Bitmap bmp = BitmapFactory.decodeByteArray(imageInByte, 0, imageInByte.length);

        mImage.setImageBitmap(bmp);

        mImage.startAnimation(zoomAnimation);

    }
}
