package com.example.tracksbasetest.utility;


import android.util.Log;

import com.example.tracksbasetest.UserAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingServ";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String notificationBody = "";
        String notificationTitle = "";
        String notificationData = "";

        try{
            notificationData = remoteMessage.getData().toString();
            notificationTitle = remoteMessage.getNotification().getTitle();
            notificationBody = remoteMessage.getNotification().getBody();
        }
        catch (NullPointerException e){
            Log.e(TAG, "onMessageReceived : NullPointerException " + e.getMessage());
        }

        Log.d(TAG, "onMessageReceived : data " + notificationData);
        Log.d(TAG, "onMessageReceived : title " + notificationTitle);
        Log.d(TAG, "onMessageReceived : body " + notificationBody);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        UserAuth.regToFCM(s);
    }
}
