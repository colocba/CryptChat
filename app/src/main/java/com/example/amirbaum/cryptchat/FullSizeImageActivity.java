package com.example.amirbaum.cryptchat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FullSizeImageActivity extends AppCompatActivity {

    public ImageView mImage;
    public StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_size_image);

        String image_name = getIntent().getStringExtra("image_name");
        final String aes_key = getIntent().getStringExtra("aes_key");

        mImage = (ImageView) findViewById(R.id.large_image_view);
        final Animation zoomAnimation = AnimationUtils.loadAnimation(this, R.anim.zoom);

        mStorage = FirebaseStorage.getInstance().getReference().child("message_images").child(image_name + ".jpg");

        mStorage.getBytes(3000*3000).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                byte[] image = EncryptionDecryptionUtility.AESdecryptMessage(bytes, aes_key);
                Bitmap bitmapImage = BitmapFactory.decodeByteArray(image, 0, image.length);
                mImage.setImageBitmap(bitmapImage);
                mImage.startAnimation(zoomAnimation);
            }
        });

    }
}
