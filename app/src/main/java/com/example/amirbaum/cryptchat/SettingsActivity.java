package com.example.amirbaum.cryptchat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    // Firebase user and reference
    private DatabaseReference mDatabaseUser;
    private FirebaseUser current_user;

    private TextView tbName;
    private TextView tbStatus;
    private Button bChangeImage;
    private Button bChangeStatus;
    private CircleImageView ciSettingsImage;
    private ProgressDialog mProgressDialog;

    // Firebase storage
    private StorageReference mStorage;

    private byte[] thum_byte;

    private static final int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        tbName = (TextView)findViewById(R.id.tbDisplayName);
        tbStatus = (TextView)findViewById(R.id.tbDisplayStatus);
        bChangeImage = (Button) findViewById(R.id.bChangeImage);
        bChangeStatus = (Button)findViewById(R.id.bChangeStatus);
        ciSettingsImage = (CircleImageView)findViewById(R.id.settings_image);

        current_user = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = current_user.getUid();
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mDatabaseUser.keepSynced(true);

        mStorage = FirebaseStorage.getInstance().getReference();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabaseUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String picture = dataSnapshot.child("picture").getValue().toString();
                String thumb = dataSnapshot.child("thumb_image").getValue().toString();

                tbName.setText(name);
                tbStatus.setText(status);
                if (!picture.equals("default"))
                {
                    //Picasso.get().load(thumb).placeholder(R.mipmap.ic_launcher).into(ciSettingsImage);
                    Picasso.get().load(picture).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.mipmap.ic_launcher).into(ciSettingsImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(picture).placeholder(R.mipmap.baseline_person_black_24).into(ciSettingsImage);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Change status click listener
        bChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = tbStatus.getText().toString();
                Intent status_intent = new Intent(SettingsActivity.this, StatusActivity.class);
                status_intent.putExtra("status_value", status);
                startActivity(status_intent);
            }
        });

        // Change picture click listener
        bChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent gallery_intent = new Intent();
                gallery_intent.setType("image/*");
                gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gallery_intent, "SELECT PICTURE"),GALLERY_PICK);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .setMinCropWindowSize(500, 500)
                    .start(SettingsActivity.this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("New Image for your profile");
                mProgressDialog.setMessage("Your image is being changed...");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                File thumb_filePath = new File(resultUri.getPath());

                try {
                    Bitmap thumb_bitmap = new Compressor(this)
                            .setQuality(75)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .compressToBitmap(thumb_filePath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    thum_byte = baos.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                // Making references to the storage of firebase
                final StorageReference thum_file_path = mStorage.child("profile_images").child("thumb").child(current_user.getUid() + ".jpg");
                final StorageReference filePath = mStorage.child("profile_images").child(current_user.getUid() + ".jpg");

                // Uploading picture and thumb to the storage
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(final Uri uri) {

                                final String downloadUrl = uri.toString();
                                UploadTask uploadTask = thum_file_path.putBytes(thum_byte);

                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        thum_file_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri_thumb) {

                                                String downloadUrl_thumb = uri_thumb.toString();
                                                Map<String, Object> updates_map = new HashMap<>();
                                                updates_map.put("picture", downloadUrl);
                                                updates_map.put("thumb_image", downloadUrl_thumb);

                                                // Uploading picture and thumb to the database
                                                mDatabaseUser.updateChildren(updates_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            mProgressDialog.dismiss();
                                                            Toasty.success(SettingsActivity.this,
                                                                    "Done uploading your new picture",
                                                                    Toast.LENGTH_SHORT,
                                                                    true).show();
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mDatabaseUser.child("online").setValue(true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseUser currentUser = current_user;

        if (currentUser == null) {
            mDatabaseUser.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }
}
