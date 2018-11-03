package com.example.amirbaum.cryptchat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import es.dmoral.toasty.Toasty;

public class StatusActivity extends AppCompatActivity {

    private TextInputEditText newStatusInputText;
    private Button bSaveChanges;
    private ProgressDialog mProgress;

    //Firebase
    DatabaseReference mDatabase;
    FirebaseUser current_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        current_user = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = current_user.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        String current_status = getIntent().getStringExtra("status_value");

        newStatusInputText = (TextInputEditText)findViewById(R.id.new_status);
        newStatusInputText.setHint(current_status);

        bSaveChanges = (Button)findViewById(R.id.save_changes);
        mProgress = new ProgressDialog(StatusActivity.this);

        bSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress.setTitle("Status");
                mProgress.setMessage("We are updating your status...");
                mProgress.show();

                String status = newStatusInputText.getEditableText().toString();
                mDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mProgress.dismiss();
                            Toasty.success(StatusActivity.this, "Your new status has been saved!", Toast.LENGTH_SHORT, true).show();
                            finish();
                        } else {
                            mProgress.dismiss();
                            Toasty.error(StatusActivity.this, "There was some error updating your status", Toast.LENGTH_SHORT, true).show();
                        }
                    }
                });
            }
        });
    }
}
