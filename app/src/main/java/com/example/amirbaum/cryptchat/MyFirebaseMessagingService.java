package com.example.amirbaum.cryptchat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.firebase.ui.auth.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by amirbaum on 30/10/2018.
 */

// IN THIS CLASS WE WILL RECEIVE NOTIFICATIONS FROM OTHER USERS AND DISPLAY IT TO THE SCREEN
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String CHANNEL_ID = "com.example.amirbaum.cryptchat.NOTIFICATIONS";
    FirebaseAuth mAuth;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            // CHECKING IF THE NOTIFICATION IS FOR THE USER THAT IS CURRENTLY CONNECTED
            if (mAuth.getCurrentUser().getUid().equals(remoteMessage.getData().get("sent"))) {
                createNotificationChannel();
                // IF THE FIELD "USER" IS AN EMPTY STRING, THAN THE NOTIFICATION IS A REQUEST FRIENDSHIP
                if (remoteMessage.getData().get("user").equals("")) {
                    showRequestNotification(remoteMessage);
                } else // THE NOTIFICATION IS A MESSAGE NOTIFICATION
                    showMessageNotification(remoteMessage);
            }
        }
    }

    // FOR ANDROID OREO VERSION PURPOSES
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notification channel name";
            String description = "Notification channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showMessageNotification(RemoteMessage remoteMessage) {

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("user_id", remoteMessage.getData().get("user"));
        intent.putExtra("my_name", remoteMessage.getData().get("to_name"));
        intent.putExtra("user_name", remoteMessage.getData().get("from_name"));

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.only_chat_bubble)
                .setContentTitle(remoteMessage.getData().get("title"))
                .setContentText(remoteMessage.getData().get("body"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(500, mBuilder.build());

    }

    private void showRequestNotification(RemoteMessage remoteMessage) {
        Intent intent = new Intent(this, UserActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.only_chat_bubble)
                .setContentTitle(remoteMessage.getData().get("title"))
                .setContentText(remoteMessage.getData().get("body"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(100, mBuilder.build());
    }
}
