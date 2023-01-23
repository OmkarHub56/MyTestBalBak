package com.myapps.bakbak;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessageNotificationService extends Service {

//    @Override
//    public void onNewToken(@NonNull String token) {
//        super.onNewToken(token);
//    }
//
//    @Override
//    public void onMessageReceived(@NonNull RemoteMessage message) {
//        super.onMessageReceived(message);
//
////        if(message.getNotification()!=null){
////            showPushNotification(message.getNotification().getTitle(),message.getNotification().getBody());
////        }
//    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
