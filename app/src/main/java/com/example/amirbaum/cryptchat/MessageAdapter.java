package com.example.amirbaum.cryptchat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.shapes.Shape;
import android.os.Environment;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Base64;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.ceylonlabs.imageviewpopup.ImagePopup;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.text.Line;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.shashank.sony.fancydialoglib.Animation;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;
import com.shashank.sony.fancydialoglib.FancyAlertDialogListener;
import com.shashank.sony.fancydialoglib.Icon;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.crypto.Cipher;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import in.myinnos.savebitmapandsharelib.SaveAndShare;

/**
 * Created by amirbaum on 10/10/2018.
 */

public class MessageAdapter extends RecyclerView.Adapter {


    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;
    private StorageReference mStorageRef;

    private String mPrivateKey;
    private String mAESKey;
    private String mMyUserId;

    private Context mContext;
    private Activity activity;

    public MessageAdapter(List<Messages> mMessageList, String privateKey, String AESKey, String myUserId,
                          Context context, Activity activity) {

        this.mMessageList = mMessageList;
        this.mPrivateKey = privateKey;
        this.mMyUserId = myUserId;
        this.mAESKey = AESKey;
        this.mContext = context;
        this.activity = activity;
    }

    @Override
    public int getItemViewType(int position) {
        Messages message = (Messages) mMessageList.get(position);

        if (message.getFrom().equals(mMyUserId)) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Messages message = (Messages) mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SenderMessageViewHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((MessageViewHolder) holder).bind(message);
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_single_from_me_layout, parent, false);
            return new SenderMessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_single_layout, parent, false);
            return new MessageViewHolder(view);
        }

        return null;
    }

    // VIEW HOLDER FOR MESSAGES SENT FROM THE OTHER USER
    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public CircleImageView profileImage;
        public TextView displayName;
        public ImageView messageImage;
        //public ImageButton newImageButton;
        public TextView timeOfTheLastMessage;
        public View mView;
        public Bitmap bitmapImage;
        public LottieAnimationView mWaitingPicAnimation;

        public MessageViewHolder(View view) {
            super(view);
            mView = view;
            messageText = (TextView) view.findViewById(R.id.message_text_layout);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_image_layout);
            displayName = (TextView) view.findViewById(R.id.name_text_layout);
            messageImage = (ImageView) view.findViewById(R.id.message_image_layout);
            //newImageButton = (ImageButton) view.findViewById(R.id.new_image_received_button);
            timeOfTheLastMessage = (TextView) view.findViewById(R.id.time_text_layout);
            mStorageRef = FirebaseStorage.getInstance().getReference();
            mWaitingPicAnimation = (LottieAnimationView) view.findViewById(R.id.waiting_pic_animation_other);
        }

        public void bind(final Messages c) {

            String from_user = c.getFrom();
            String message_type = c.getType();


            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
            mUserDatabase.keepSynced(true);

            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String name = dataSnapshot.child("name").getValue().toString();
                    String image = dataSnapshot.child("picture").getValue().toString();

                    displayName.setText(name);

                    Picasso.get().load(image)
                            .placeholder(R.drawable.user_place_holder).into(profileImage);
                    timeOfTheLastMessage.setText(convertStampToTime(c.getTime()));

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            // THE MESSAGE IS A TEXT MESSAGE
            if (message_type.equals("text")) {

                messageText.setVisibility(View.VISIBLE);
                messageImage.setVisibility(View.GONE);
                mWaitingPicAnimation.setVisibility(View.GONE);

                String decryptedMessage = "";

                decryptedMessage = EncryptionDecryptionUtility.RSAdecryptMessage(c.getRsa_encrypted_message(), mPrivateKey);
                messageText.setText(decryptedMessage);

                // THE MESSAGE IS AN IMAGE MESSAGE
            } else {
                // rsa_encrypted_message contains the encoded encrypted aes key for the other user to open

                mWaitingPicAnimation.setVisibility(View.VISIBLE);
                messageImage.setVisibility(View.GONE);
                messageText.setVisibility(View.GONE);

                final String messageId = c.getId();
                StorageReference filePath = mStorageRef.child("message_images").child(messageId + ".jpg");

                String encodedEncryptedAESKey = c.getRsa_encrypted_message();
                final String aesKey = EncryptionDecryptionUtility.RSAdecryptMessage(encodedEncryptedAESKey, mPrivateKey);

                filePath.getBytes(3000*3000).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {

                        // COMPRESSING IMAGE TO DISPLAY ON SCREEN
                        byte[] image = EncryptionDecryptionUtility.AESdecryptMessage(bytes, aesKey);
                        bitmapImage = BitmapFactory.decodeByteArray(image, 0, image.length);
                        messageImage.setImageBitmap(bitmapImage);
                        mWaitingPicAnimation.setVisibility(View.GONE);
                        messageImage.setVisibility(View.VISIBLE);
                    }
                });

                messageImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ImagePopup imagePopup = new ImagePopup(mContext);
                        imagePopup.setWindowHeight(800); // Optional
                        imagePopup.setWindowWidth(800); // Optional
                        imagePopup.setBackgroundColor(Color.BLACK);  // Optional
                        imagePopup.setFullScreen(true); // Optional
                        imagePopup.setHideCloseIcon(true);  // Optional
                        imagePopup.setImageOnClickClose(true);  // Optiona
                        imagePopup.initiatePopup(messageImage.getDrawable());
                        imagePopup.viewPopup();
                    }
                });

                messageImage.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        new FancyAlertDialog.Builder(activity)
                                .setTitle("Store this image")
                                .setBackgroundColor(Color.parseColor("#303F9F"))  //Don't pass R.color.colorvalue
                                .setMessage("Are you sure you want to store this image?")
                                .setNegativeBtnText("Cancel")
                                .setPositiveBtnBackground(Color.parseColor("#FF4081"))  //Don't pass R.color.colorvalue
                                .setPositiveBtnText("Sure!")
                                .setNegativeBtnBackground(Color.parseColor("#FFA9A7A8"))  //Don't pass R.color.colorvalue
                                .setAnimation(Animation.SLIDE)
                                .isCancellable(true)
                                .setIcon(R.drawable.ic_photo_library, Icon.Visible)
                                .OnPositiveClicked(new FancyAlertDialogListener() {
                                    @Override
                                    public void OnClick() {
                                        SaveAndShare.save(activity, bitmapImage,
                                                messageId + ".jpg", "The picture has been saved on the phone", null);
                                    }
                                })
                                .OnNegativeClicked(new FancyAlertDialogListener() {
                                    @Override
                                    public void OnClick() {
                                        return;
                                    }
                                })
                                .build();
                        return false;
                    }
                });

            }

        }
    }

    // VIEW HOLDER FOR MESSAGES SENT FROM ME
    public class SenderMessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public ImageView messageImage;
        public TextView timeOfTheLastMessage;
        public View mView;
        public Bitmap bitmapImage;
        public LottieAnimationView mWaitingPicAnimation;

        public SenderMessageViewHolder(View view) {
            super(view);
            mView = view;
            messageText = (TextView) view.findViewById(R.id.message_text_layout);
            //profileImage = (CircleImageView) view.findViewById(R.id.message_profile_image_layout);
            messageImage = (ImageView) view.findViewById(R.id.message_image_layout);
            mWaitingPicAnimation = (LottieAnimationView) view.findViewById(R.id.waiting_pic_animation);
            //newImageButton = (ImageButton) view.findViewById(R.id.new_image_received_button);
            timeOfTheLastMessage = (TextView) view.findViewById(R.id.time_text_layout);
            mStorageRef = FirebaseStorage.getInstance().getReference();
        }

        public void bind(final Messages c) {

            String from_user = c.getFrom();
            final String message_type = c.getType();


            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
            mUserDatabase.keepSynced(true);

            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    timeOfTheLastMessage.setText(convertStampToTime(c.getTime()));

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            // THE MESSAGE IS A TEXT MESSAGE
            if (message_type.equals("text")) {
                messageText.setVisibility(View.VISIBLE);
                messageImage.setVisibility(View.GONE);
                mWaitingPicAnimation.setVisibility(View.GONE);


                String decryptedMessage = "";

                decryptedMessage = EncryptionDecryptionUtility.AESdecryptMessage(c.getAes_encrypted_message(), mAESKey);
                messageText.setText(decryptedMessage);

                // THE MESSAGE IS AN IMAGE MESSAGE
            } else {
                // rsa_encrypted_message contains the encoded encrypted aes key for the other user to open
                messageImage.setVisibility(View.GONE);
                mWaitingPicAnimation.setVisibility(View.VISIBLE);
                messageText.setVisibility(View.GONE);

                final String messageId = c.getId();
                StorageReference filePath = mStorageRef.child("message_images").child(messageId + ".jpg");

                filePath.getBytes(3000*3000).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // COMPRESSING IMAGE TO DISPLAY ON SCREEN
                        byte[] image = EncryptionDecryptionUtility.AESdecryptMessage(bytes, mAESKey);
                        bitmapImage = BitmapFactory.decodeByteArray(image, 0, image.length);
                        messageImage.setImageBitmap(bitmapImage);
                        mWaitingPicAnimation.setVisibility(View.GONE);
                        messageImage.setVisibility(View.VISIBLE);
                    }
                });

                messageImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ImagePopup imagePopup = new ImagePopup(mContext);
                        imagePopup.setWindowHeight(800); // Optional
                        imagePopup.setWindowWidth(800); // Optional
                        imagePopup.setBackgroundColor(Color.BLACK);  // Optional
                        imagePopup.setFullScreen(true); // Optional
                        imagePopup.setHideCloseIcon(true);  // Optional
                        imagePopup.setImageOnClickClose(true);  // Optiona
                        imagePopup.initiatePopup(messageImage.getDrawable());
                        imagePopup.viewPopup();
                    }
                });

            }

        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    // HELP FUNCTIONS
    private String convertStampToTime(long time) {
        DateFormat df = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
        String text = df.format(new Date(time));
        return text;
    }

    private byte[] convertBitmapToByteArray(Bitmap bitmapImage) {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();

    }

    public void clear() {
        final int size = mMessageList.size();
        mMessageList.clear();
        notifyItemRangeRemoved(0, size);
    }


}
