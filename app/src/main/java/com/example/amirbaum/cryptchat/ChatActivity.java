package com.example.amirbaum.cryptchat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    // Custom bar widgets
    private Toolbar mChatToolBar;
    private TextView mCustomBarTitle;
    private TextView mCustomBarSeen;
    private CircleImageView mCustomBarImageView;
    private RecyclerView mMessagesList;
    //private SwipeRefreshLayout mRefreshLayout;

    // Bottom bar widgets
    private ImageButton mAddButtonChat;
    private EditText mEditTextMessage;
    private ImageButton mSendButtonChat;

    // Firebase
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;

    // Storage Firebase
    private StorageReference mImageStorage;

    // Global members
    private String mChatUserName;
    private String mChatUserId;
    private String mPicture;
    private String mOnline;
    private String mCurrentUserId;

    // PRIVATE AND PUBLIC KEYS
    private String mPublicKey;
    private String mPrivateKey;
    private String mAESKey;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private static final int GALLERY_PICK = 1;

    // Refreshing paging purposes
    private int mCurrentPage = 1;
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatToolBar = (Toolbar) findViewById(R.id.chat_bar_app);
        setSupportActionBar(mChatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mImageStorage = FirebaseStorage.getInstance().getReference();

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        SharedPreferences sp = getBaseContext().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        mPrivateKey = sp.getString("private_key_" + mCurrentUserId, "");
        mAESKey = sp.getString("aes_key_" + mCurrentUserId, "");

        mChatUserId = getIntent().getStringExtra("user_id");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        mCustomBarTitle = (TextView) findViewById(R.id.custom_bar_title);
        mCustomBarSeen = (TextView) findViewById(R.id.custom_bar_seen);
        mCustomBarImageView = (CircleImageView) findViewById(R.id.custom_bar_image);

        mAddButtonChat = (ImageButton) findViewById(R.id.chat_add_button);
        mEditTextMessage = (EditText) findViewById(R.id.chat_message_view);
        mSendButtonChat = (ImageButton) findViewById(R.id.chat_send_button);

        mAdapter = new MessageAdapter(messagesList, mPrivateKey, mAESKey, mCurrentUserId, getApplicationContext());

        mMessagesList = (RecyclerView) findViewById(R.id.messages_list);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);

        loadMessages();


        // Retrieving all information of chat user in order to initialize widgets and the public key for encryption
        mRootRef.child("Users").child(mChatUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mChatUserName = dataSnapshot.child("name").getValue().toString();
                mPicture = dataSnapshot.child("picture").getValue().toString();
                mOnline = dataSnapshot.child("online").getValue().toString();
                mPublicKey = dataSnapshot.child("public_key").getValue().toString();
                mCustomBarTitle.setText(mChatUserName);
                if (!mPicture.equals("default")) {
                    Picasso.get().load(mPicture).placeholder(R.drawable.user_place_holder).into(mCustomBarImageView);
                }

                if (mOnline.equals("true")) {

                    mCustomBarSeen.setText("Online");

                } else {

                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(mOnline);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                    mCustomBarSeen.setText(lastSeenTime);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Updating the Chat field of our database according to both users
        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(mChatUserId)) {
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUserId, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUserId + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
                            }
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Listener for Add button
        mAddButtonChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery_intent = new Intent();
                gallery_intent.setType("image/*");
                gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gallery_intent, "SELECT PICTURE"), GALLERY_PICK);
            }
        });

        // Listener for Send button
        mSendButtonChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();

            }
        });

        mCustomBarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selected_user_intent = new Intent(ChatActivity.this, ProfileActivity.class);
                selected_user_intent.putExtra("user_id", mChatUserId);
                startActivity(selected_user_intent);
            }
        });

    }

    // Here we received the cropped picture that the user picked for sending to the other user
    // IN ORDER TO MAKE ENCRYPTION WE WILL ENCRYPT THE PICTURE WITH AES KEY AND WE WILL ENCRYPT
    // THE AES KEY WITH THE PUBLIC KEY.
    // WE WILL UPLOAD THAT ENCRYPTED (WITH RSA) KEY TO FIREBASE.
    // THE OTHER USER HAVE TO DECRYPT THE ENCRYPTED (WITH RSA) AES KEY, AND THEN DECRYPT THE IMAGE WITH
    // THE AES KEY.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();
            byte[] image_byte = readBytes(imageUri);

            // AES ENCRYPTION OF THE IMAGE
            final byte[] AESencryptedImage = EncryptionDecryptionUtility.AESencryptMessage(image_byte, mAESKey);
            // RSA ENCRYPTION OF THE KEY
            final byte[] encryptedAESKey = EncryptionDecryptionUtility.RSAencryptMessage(mAESKey, mPublicKey);
            final String encodedEncryptedAESKey = Base64.encodeToString(encryptedAESKey, Base64.DEFAULT);

            final String current_user_ref = "Messages/" + mCurrentUserId + "/" + mChatUserId;
            final String chat_user_ref = "Messages/" + mChatUserId + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("Messages")
                    .child(mCurrentUserId).child(mChatUserId).push();

            final String push_id = user_message_push.getKey();
            final StorageReference filepath_aes = mImageStorage.child("message_images").child(push_id + ".jpg");

            // UPLOADING THE IMAGE ENCRYPTED WITH AES KEY
            filepath_aes.putBytes(AESencryptedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Map messageMap = new HashMap();
                    messageMap.put("rsa_encrypted_message", encodedEncryptedAESKey);
                    messageMap.put("aes_encrypted_message", "");
                    messageMap.put("seen", false);
                    messageMap.put("type", "image");
                    messageMap.put("time", ServerValue.TIMESTAMP);
                    messageMap.put("from", mCurrentUserId);
                    messageMap.put("id", push_id);

                    Map messageUserMap = new HashMap();
                    messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                    messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                    mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null) {

                                Log.d("CHAT_LOG", databaseError.getMessage().toString());

                            }

                        }
                    });
                }
            });


        }

    }

    private void loadMoreMessages() {

        DatabaseReference messageRef = mRootRef.child("Messages").child(mCurrentUserId).child(mChatUserId);
        Query messagesQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messagesQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                if (!mPrevKey.equals(messageKey)) {

                    messagesList.add(itemPos++, message);

                } else {
                    mPrevKey = mLastKey;
                }

                if (itemPos == 1) {

                    mLastKey = messageKey;

                }


                mAdapter.notifyDataSetChanged();

                //mRefreshLayout.setRefreshing(false);

                //mLinearLayout.scrollToPositionWithOffset(10, 0);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    // This method loads images on recycler view
    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("Messages").child(mCurrentUserId).child(mChatUserId);
        Query messagesQuery = messageRef.limitToLast(TOTAL_ITEMS_TO_LOAD);


        messagesQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages message = dataSnapshot.getValue(Messages.class);
                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messagesList.size() - 1);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    // This method only updates the database with the new encrypted message
    private void sendMessage() {

        String message = mEditTextMessage.getText().toString();


        if (!TextUtils.isEmpty(message)) {

            // RSA ENCRYPTION OF THE MESSAGE
            byte[] RSAencryptedMessage = EncryptionDecryptionUtility.RSAencryptMessage(message, mPublicKey);
            String RSAencodedEncryptedMessage = Base64.encodeToString(RSAencryptedMessage, Base64.DEFAULT);

            // AES ENCRYPTION OF THE MESSAGE
            byte[] AESencryptedMessage = EncryptionDecryptionUtility.AESencryptMessage(message, mAESKey);
            String AESencodedEncryptedMessage = Base64.encodeToString(AESencryptedMessage, Base64.DEFAULT);

            String current_user_ref = "Messages/" + mCurrentUserId + "/" + mChatUserId;
            String chat_user_ref = "Messages/" + mChatUserId + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("Messages").child(mCurrentUserId)
                    .child(mChatUserId).push();

            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("rsa_encrypted_message", RSAencodedEncryptedMessage);
            messageMap.put("aes_encrypted_message", AESencodedEncryptedMessage);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            mEditTextMessage.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                    if (databaseError != null) {
                        Log.d("CHAT_LOG", databaseError.getMessage().toString());
                    }

                }
            });

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mRootRef.child("Users").child(mCurrentUserId).child("online").setValue(true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            mRootRef.child("Users").child(mCurrentUserId).child("online").setValue(ServerValue.TIMESTAMP);
        }

    }

    public byte[] readBytes(Uri uri) {
        try {

            // this dynamically extends to take the bytes you read
            InputStream inputStream = getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

            // this is storage overwritten on each iteration with bytes
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            // we need to know how may bytes were read to write them to the byteBuffer
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }

            // and then we can return your byte array.
            return byteBuffer.toByteArray();

        } catch (IOException e) {
            Log.d("READ_BYTE_ERROR", e.toString());
        }
        return null;
    }

}
