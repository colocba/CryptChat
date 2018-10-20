package com.example.amirbaum.cryptchat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.FaceDetector;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etEmail;
    private EditText etPassword;
    private Button bSignIn;
    private Button bRegister;
    private ImageView logo;
    private ImageView bFaceReco;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;

    private ProgressDialog progressDialogLoggin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        logo = (ImageView)findViewById(R.id.logo_cryptchat);
        Animation fadeinLogo = AnimationUtils.loadAnimation(this, R.anim.fadein);
        logo.startAnimation(fadeinLogo);

        etEmail = (EditText)findViewById(R.id.editTextEmail);
        etPassword = (EditText)findViewById(R.id.editTextPassword);
        bSignIn = (Button)findViewById(R.id.bSignIn);
        bRegister = (Button)findViewById(R.id.bRegister);
        bFaceReco = (ImageView)findViewById(R.id.face_reco_button);

        bSignIn.setOnClickListener(this);
        bRegister.setOnClickListener(this);
        bFaceReco.setOnClickListener(this);

        progressDialogLoggin = new ProgressDialog(this);
        getSupportActionBar().hide();

    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            Intent user_intent = new Intent(MainActivity.this, UserActivity.class);
            startActivity(user_intent);
        }
    }


    @Override
    public void onClick(View v) {
        if (v == bSignIn) {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toasty.info(this, "Enter an email to register", Toast.LENGTH_SHORT, true).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toasty.info(this, "Enter a password for your new user", Toast.LENGTH_SHORT, true).show();
                return;
            }

            progressDialogLoggin.setTitle("Logging In");
            progressDialogLoggin.setMessage("We are checking your identity.");
            progressDialogLoggin.show();

            loggIn(email, password);

        }

        if (v == bRegister) {
            Intent register_intent = new Intent(MainActivity.this, Register.class);
            startActivity(register_intent);
        }

        if (v == bFaceReco) {
            // TODO: MAKE FACE RECOGNITION AUTHENTICATION
        }
    }


    private void loggIn(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    final String current_user_id = mAuth.getCurrentUser().getUid();
                    String device_token = FirebaseInstanceId.getInstance().getToken();

                    // UPLOADING TOKEN AND PUBLIC KEY TO FIREBASE
                    Map loginMap = new HashMap();
                    loginMap.put(current_user_id + "/" + "device_token", device_token);

                    mDatabaseUsers.updateChildren(loginMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            progressDialogLoggin.dismiss();
                            Intent user_intent = new Intent(MainActivity.this, UserActivity.class);
                            startActivity(user_intent);
                        }
                    });


                } else {
                    progressDialogLoggin.hide();
                    Toasty.error(MainActivity.this, "Wrong email or password", Toast.LENGTH_SHORT, true).show();
                    return;
                }
            }
        });
    }

}
