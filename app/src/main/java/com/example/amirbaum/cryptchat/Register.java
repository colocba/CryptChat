package com.example.amirbaum.cryptchat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.KeyGenerator;

import es.dmoral.toasty.Toasty;

public class Register extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button bRegister;
    private ImageButton bTakePic;
    private ProgressDialog progressDialog;
    private DatabaseReference mDataBase;
    private StorageReference mStorageRef;
    private Bitmap mImageForFaceReco;

    private final String FIREBASE_NAME_FIELD = "name";
    private final String FIREBASE_STATUS_FIELD = "status";
    private final String FIREBASE_PICTURE_FIELD = "picture";
    private final String FIREBASE_THUMB_IMAGE_FIELD = "thumb_image";
    private final String FIREBASE_TOKEN_FIELD = "device_token";
    private final String FIREBASE_ONLINE_FIELD = "online";
    private final String FIREBASE_PUBLIC_KEY_FIELD = "public_key";
    private final String FIREBASE_FACE_RECO_FIELD = "face_reco_picture";

    private static final int CAMERA_PIC_REQUEST = 1337;

    private SharedPreferences mSharedPref;
    private SharedPreferences.Editor mEditor;

    private boolean mPictureTaken = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        etName = (EditText)findViewById(R.id.name);
        etEmail = (EditText)findViewById(R.id.email);
        etPassword = (EditText)findViewById(R.id.password);
        etConfirmPassword = (EditText)findViewById(R.id.confirmPassword);
        bRegister = (Button)findViewById(R.id.register);
        bTakePic = (ImageButton)findViewById(R.id.take_pic);

        bRegister.setOnClickListener(this);
        bTakePic.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
    }

    @Override
    public void onClick(View v) {

        // TODO: CHANGE IF WE DONT DO FACE RECO

        if (v == bRegister) {
            // TODO: ERASE IF WE DONT DO FACE RECO
            /*if (mPictureTaken == false) {
                Toasty.info(this, "Please, take a picture first", Toast.LENGTH_SHORT, true).show();
                return;
            }*/
            registerUser();
        }
        if (v == bTakePic) {
            takePicture();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // TODO: ERASE IF WE DONT DO FACE RECO

        if (requestCode == CAMERA_PIC_REQUEST) {
            mImageForFaceReco = (Bitmap) data.getExtras().get("data");
            mPictureTaken = true;
            Toast.makeText(this, "RECEIVED PIC", Toast.LENGTH_SHORT).show();
        }
    }

    private void takePicture() {

        // TODO: ERASE IF WE DONT DO FACE RECO

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);

    }

    public void registerUser() {

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Checking that each field has been entered
        if (TextUtils.isEmpty(name)) {
            Toasty.info(this, "Enter your name", Toast.LENGTH_SHORT, true).show();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            Toasty.info(this, "Enter an email to register", Toast.LENGTH_SHORT, true).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toasty.info(this, "Enter a password", Toast.LENGTH_SHORT, true).show();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            Toasty.info(this, "Enter the new password again", Toast.LENGTH_SHORT, true).show();
            return;
        }

        if (!checkBothPasswords(password, confirmPassword)) {
            Toasty.error(this, "First password doesn't match with second password", Toast.LENGTH_SHORT, true).show();
            return;
        }


        progressDialog.setMessage("Registering new user...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        // Creating new user in firebase, and entering all his data to the database according to his uid
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();
                    final String device_token = FirebaseInstanceId.getInstance().getToken();
                    //final StorageReference face_pic_path = mStorageRef.child("face_reco_pictures").child(current_user.getUid() + ".jpg");

                    KeysGenerator keysGenerator = new KeysGenerator();
                    keysGenerator.generateRSAKey();
                    keysGenerator.generateAESKey();
                    final String publicKey = keysGenerator.getPublicKey();
                    String privateKey = keysGenerator.getPrivateKey();
                    String AESKey = keysGenerator.getAESKey();


                    // SAVING THE AES AND PUBLIC KEY ON THE DEVICE AS A STRING
                    mSharedPref = getSharedPreferences("myPref", Context.MODE_PRIVATE);
                    mEditor = mSharedPref.edit();
                    mEditor.putString("private_key_" + uid, privateKey);
                    mEditor.putString("aes_key_" + uid, AESKey);
                    mEditor.commit();

                    mDataBase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    // SETTING A MAP OF ALL DATA OF THE NEW USER REGISTERED
                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put(FIREBASE_NAME_FIELD, etName.getText().toString());
                    userMap.put(FIREBASE_STATUS_FIELD, "Hello there!");
                    userMap.put(FIREBASE_PICTURE_FIELD, "default");
                    userMap.put(FIREBASE_THUMB_IMAGE_FIELD, "default");
                    userMap.put(FIREBASE_TOKEN_FIELD, device_token);
                    userMap.put(FIREBASE_ONLINE_FIELD, "false");
                    // SAVING THE PUBLIC KEY ON FIREBASE DATABASE AS A STRING
                    userMap.put(FIREBASE_PUBLIC_KEY_FIELD, publicKey);
                    // SAVING FACE RECOGNITION PICTURE FOR USER LATER TO LOGIN
                    //userMap.put(FIREBASE_FACE_RECO_FIELD, download_uri);

                    // UPLOADING EVERYTHING TO FIREBASE
                    mDataBase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toasty.success(Register.this, "You have been successfully registered", Toast.LENGTH_SHORT, true).show();
                                progressDialog.cancel();
                            } else
                                return;
                        }
                    });


                    // UPLOADING THE IMAGE RECOGNITION PICTURE FIRST
                    // TODO: ERRASE IF WE DONT DO FACE RECO
                    /*face_pic_path.putBytes(bitmapToByteArray(mImageForFaceReco)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            face_pic_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String download_uri = uri.toString();


                                }
                            });
                        }
                    });*/

                } else {
                    Toasty.error(Register.this, task.getResult().toString(), Toast.LENGTH_SHORT, true).show();
                    progressDialog.cancel();
                }

            }
        });

    }

    private byte[] bitmapToByteArray(Bitmap mImageForFaceReco) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mImageForFaceReco.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        return data;
    }

    public boolean checkBothPasswords(String pass1, String pass2) {
        if (pass1.equals(pass2))
            return true;
        else
            return false;
    }

}
