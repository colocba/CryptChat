package com.example.amirbaum.cryptchat;

import android.app.ProgressDialog;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class ProfileActivity extends AppCompatActivity {

    // Widgets variables
    private ImageView userImageView;
    private TextView userNameTextView;
    private TextView userStatusTextView;
    private Button requestFriendshipButton, declineFriendshipButton;
    private ProgressDialog progressDialog;

    // Firebase database reference
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseFriendRequest;
    private DatabaseReference mDatabaseFriends;
    private DatabaseReference mDatabaseNotifications;
    private DatabaseReference mRootRef;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAuth;

    private String mCurrentState;
    private String mUserName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Getting the users id we are looking now
        final String user_id = getIntent().getStringExtra("user_id");
        // Getting my name for filter purposes
        final String myName = getIntent().getStringExtra("name");
        // Getting our user instance
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();


        //------------------ Starting all widgets ---------------------- //
        userImageView = (ImageView) findViewById(R.id.profile_image_view);
        userNameTextView = (TextView) findViewById(R.id.profile_name_text);
        userStatusTextView = (TextView) findViewById(R.id.profile_status_text);
        requestFriendshipButton = (Button) findViewById(R.id.send_request_button);
        declineFriendshipButton = (Button) findViewById(R.id.decline_friendship_button);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading User's profile");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        mCurrentState = "not_friends";

        declineFriendshipButton.setVisibility(View.INVISIBLE);
        declineFriendshipButton.setEnabled(false);

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mDatabaseFriendRequest = FirebaseDatabase.getInstance().getReference().child("Friends_Req");
        mDatabaseFriends = FirebaseDatabase.getInstance().getReference().child("Friends");
        mDatabaseNotifications = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mUserName = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String picture = dataSnapshot.child("picture").getValue().toString();

                userNameTextView.setText(mUserName);
                userStatusTextView.setText(status);

                if (!picture.equals("default")) {
                    Picasso.get().load(picture).placeholder(R.drawable.user_place_holder).into(userImageView);
                }

                // Listening on the Friends_Req -> ME field of the firebase db
                mDatabaseFriendRequest.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        // Or I have sent this user a request, or the friend sent me a request
                        if (dataSnapshot.hasChild(user_id)) {

                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            // The user sent me a request ----------
                            if (req_type.equals("received")) {
                                // THE USER WE ARE LOOKING NOW HAS SEND US A FRIEND REQUEST
                                mCurrentState = "req_received";
                                requestFriendshipButton.setText("ACCEPT REQUEST");

                                declineFriendshipButton.setVisibility(View.VISIBLE);
                                declineFriendshipButton.setEnabled(true);

                                // I sent a request to the user ---------
                            } else if (req_type.equals("sent")) {
                                // WE HAVE SENT A REQUEST TO THE USER WE ARE LOOKING NOW
                                mCurrentState = "req_sent";
                                requestFriendshipButton.setText("Cancel request");

                                declineFriendshipButton.setVisibility(View.INVISIBLE);
                                declineFriendshipButton.setEnabled(false);
                            }

                            progressDialog.dismiss();

                            // There is no request between me and the user
                        } else {

                            mDatabaseFriends.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)) {
                                        mCurrentState = "friends";
                                        requestFriendshipButton.setText("Unfriend");

                                        declineFriendshipButton.setVisibility(View.INVISIBLE);
                                        declineFriendshipButton.setEnabled(false);
                                    }

                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                    progressDialog.dismiss();

                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toasty.error(ProfileActivity.this, "There has been some error loading this user's profile", Toast.LENGTH_SHORT, true).show();
            }
        });

        requestFriendshipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestFriendshipButton.setEnabled(false);

                // ------------------  NOT FRIENDS STATE --------------------//
                if (mCurrentState.equals("not_friends")) {

                    DatabaseReference newNotificationRef = mRootRef.child("Notifications").child(user_id).push();
                    String newNotificationId = newNotificationRef.getKey();

                    HashMap<String, String> dataMap = new HashMap<>();
                    dataMap.put("from", mCurrentUser.getUid());
                    dataMap.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friends_Req/" + mCurrentUser.getUid() + "/" + user_id + "/request_type", "sent");
                    //requestMap.put("Friends_Req/" + mCurrentUser.getUid() + "/" + user_id + "/from", mUserName);
                    requestMap.put("Friends_Req/" + user_id + "/" + mCurrentUser.getUid() + "/request_type", "received");
                    //requestMap.put("Friends_Req/" + user_id + "/" + mCurrentUser.getUid() + "/from", myName);
                    requestMap.put("Notifications/" + user_id + "/" + newNotificationId, dataMap);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if (databaseError != null) {
                                Toasty.error(ProfileActivity.this, "There was some error", Toast.LENGTH_SHORT, true).show();
                            }

                            requestFriendshipButton.setEnabled(true);

                            mCurrentState = "req_sent";
                            requestFriendshipButton.setText("Cancel friend request");

                        }
                    });

                    // ------------------  CANCEL REQUEST STATE --------------------//
                } else if (mCurrentState.equals("req_sent")) {

                    removeFriendShipRequestOfTwoUsersFromDB(mDatabaseFriendRequest, mCurrentUser.getUid(), user_id, "not_friends",
                            "Request friendship");

                    declineFriendshipButton.setVisibility(View.INVISIBLE);
                    declineFriendshipButton.setEnabled(false);

                    Toasty.success(ProfileActivity.this, "Your request has been canceled", Toast.LENGTH_SHORT, true).show();

                } else if (mCurrentState.equals("req_received")) {

                    final String currentDate = DateFormat.getDateInstance().format(new Date());

                    Map friendsMap = new HashMap();

                    // Entering the date of the recent friendship in respective friends fields
                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id + "/date", currentDate);
                    // FOR FILTERING PURPOSES
                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id + "/friends_name", mUserName);
                    friendsMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid() + "/date", currentDate);
                    // FOR FILTERING PURPOSES
                    friendsMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid() + "/friends_name", myName);

                    // Deleting friend request between both of the users
                    friendsMap.put("Friends_Req/" + mCurrentUser.getUid() + "/" + user_id, null);
                    friendsMap.put("Friends_Req/" + user_id + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if (databaseError == null) {
                                requestFriendshipButton.setEnabled(true);
                                mCurrentState = "friends";
                                requestFriendshipButton.setText("Unfriend this person");

                                declineFriendshipButton.setVisibility(View.INVISIBLE);
                                declineFriendshipButton.setEnabled(false);
                            }

                            else {
                                Toasty.error(ProfileActivity.this, "There was an error accepting this request",
                                        Toast.LENGTH_SHORT, true).show();
                            }

                        }
                    });


                } else if (mCurrentState.equals("friends")) {

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if (databaseError == null) {

                                mCurrentState = "not_friends";
                                requestFriendshipButton.setText("Send friend request");

                                declineFriendshipButton.setVisibility(View.INVISIBLE);
                                declineFriendshipButton.setEnabled(false);
                            }

                            else {
                                Toasty.error(ProfileActivity.this, "There was an error accepting this request",
                                        Toast.LENGTH_SHORT, true).show();
                            }

                            requestFriendshipButton.setEnabled(true);

                        }
                    });

                }

            }
        });
    }

    public void removeFriendShipRequestOfTwoUsersFromDB(final DatabaseReference databaseFriendRequest, final String current_user_id,
                                                        final String profile_user_id, final String state, final String buttonText) {
        databaseFriendRequest.child(current_user_id).child(profile_user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                databaseFriendRequest.child(profile_user_id).child(current_user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        requestFriendshipButton.setEnabled(true);
                        mCurrentState = state;
                        requestFriendshipButton.setText(buttonText);
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mRootRef.child("Users").child(mCurrentUser.getUid()).child("online").setValue(true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            mRootRef.child("Users").child(mCurrentUser.getUid()).child("online").setValue(ServerValue.TIMESTAMP);
        }

    }
}
