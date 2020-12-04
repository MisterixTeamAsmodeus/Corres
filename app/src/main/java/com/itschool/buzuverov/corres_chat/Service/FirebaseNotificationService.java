package com.itschool.buzuverov.corres_chat.Service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.itschool.buzuverov.corres_chat.Activity.SplashScreenActivity;
import com.itschool.buzuverov.corres_chat.R;

import java.util.Map;

public class FirebaseNotificationService extends FirebaseMessagingService {

    private final static String CHANEL_ID = "com.itschool.buzuverov.corres_chat";
    private final static String CHANEL_NAME = "Notification";
    private NotificationManager notificationManager;
    private String authUserId;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        authUserId = FirebaseAuth.getInstance().getUid();
        if (authUserId != null){
            SharedPreferences preferences = getSharedPreferences("OpenUserInfo", Context.MODE_PRIVATE);
            if (!preferences.getBoolean("openChat",false)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendOreoNotification(remoteMessage.getData());
                } else {
                    sendNotification(remoteMessage.getData());
                }
            } else {
                if (!preferences.getString("openedUserId","").equals(remoteMessage.getData().get("userId"))){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        sendOreoNotification(remoteMessage.getData());
                    } else {
                        sendNotification(remoteMessage.getData());
                    }
                }
            }
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        String authUserId = FirebaseAuth.getInstance().getUid();
        if (authUserId != null)
            FirebaseDatabase.getInstance().getReference().child("User").child(authUserId).child("token").setValue(token);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendOreoNotification(Map<String, String> messageBody) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationChannel channel = new NotificationChannel(CHANEL_ID, CHANEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{200, 500});
        channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
        channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        getManager().createNotificationChannel(channel);
        Intent intent = new Intent(this, SplashScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        if (messageBody.get("type").equals("message") || messageBody.get("type").equals("updateMessage")){
            int j = Integer.parseInt(messageBody.get("userId").replaceAll("[\\D]", ""));
            String[] allMessageText = messageBody.get("messageText").split(String.valueOf((char)(1)));
            String[] allMessageId = messageBody.get("messageId").split(" ");
            String lastMessage = allMessageText[allMessageText.length - 1].replaceAll("'","");
            String message = "";
            for(int i = 0; i < allMessageText.length; i++){
                allMessageText[i] = allMessageText[i].replaceAll("'","");
                if (i != allMessageText.length - 1)
                    message += allMessageText[i] + "\n";
                else
                    message += allMessageText[i];
            }
            if (message.length() == 0){
                getManager().cancel(j);
            } else {
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANEL_ID)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setContentTitle(messageBody.get("userName") + " прислал вам сообщение")
                        .setContentText(lastMessage)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setAutoCancel(true)
                        .setLights(Color.YELLOW, 500, 5000)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setContentIntent(pendingIntent);

                getManager().notify(j, notificationBuilder.build());
                try {
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), defaultSoundUri);
                    r.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (messageBody.get("type").equals("message"))
                    FirebaseDatabase.getInstance().getReference().child("Messages").child(messageBody.get("userId")).child(authUserId).child(allMessageId[allMessageId.length - 1]).child("status").setValue("served");
            }
        } else if (messageBody.get("type").equals("newRequest")){
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANEL_ID)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setContentTitle("Новый запрос в друзья")
                    .setContentText(messageBody.get("userName") + " хочет добавить вас в друзья")
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setAutoCancel(true)
                    .setLights(Color.YELLOW, 500, 5000)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody.get("userName") + " хочет добавить вас в друзья"))
                    .setContentIntent(pendingIntent);
            int j = Integer.parseInt(messageBody.get("userId").replaceAll("[\\D]", "")) + 2;
            getManager().notify(j, notificationBuilder.build());
            try {
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), defaultSoundUri);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (messageBody.get("type").equals("cancelRequest")){
            int j = Integer.parseInt(messageBody.get("userId").replaceAll("[\\D]", "")) + 2;
            getManager().cancel(j);
        }
    }

    private void sendNotification(Map<String, String> messageBody){
        Intent intent = new Intent(this, SplashScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        switch (messageBody.get("type")) {
            case "message":
            case "updateMessage": {
                int j = Integer.parseInt(messageBody.get("userId").replaceAll("[\\D]", ""));
                String[] allMessageText = messageBody.get("messageText").split(String.valueOf((char) (1)));
                String[] allMessageId = messageBody.get("messageId").split(" ");
                String lastMessage = allMessageText[allMessageText.length - 1].replaceAll("'", "");
                StringBuilder message = new StringBuilder();
                for (int i = 0; i < allMessageText.length; i++) {
                    allMessageText[i] = allMessageText[i].replaceAll("'", "");
                    if (i != allMessageText.length - 1)
                        message.append(allMessageText[i]).append("\n");
                    else
                        message.append(allMessageText[i]);
                }
                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(this, CHANEL_ID)
                                .setSmallIcon(R.drawable.ic_stat_name)
                                .setColor(getResources().getColor(R.color.colorPrimary))
                                .setContentTitle(messageBody.get("userName") + " прислал вам сообщение")
                                .setContentText(lastMessage)
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setAutoCancel(true)
                                .setSound(defaultSoundUri)
                                .setLights(Color.YELLOW, 500, 5000)
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .setVibrate(new long[]{200, 500})
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(message.toString()))
                                .setContentIntent(pendingIntent);
                getManager().notify(j, notificationBuilder.build());
                if (messageBody.get("type").equals("message"))
                    FirebaseDatabase.getInstance().getReference().child("Messages").child(messageBody.get("userId")).child(authUserId).child(allMessageId[allMessageId.length - 1]).child("status").setValue("served");
                break;
            }
            case "newRequest": {
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANEL_ID)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setContentTitle(messageBody.get("userName") + " прислал вам сообщение")
                        .setContentText(messageBody.get("userName") + " хочет добавить вас в друзья")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setLights(Color.YELLOW, 500, 5000)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setVibrate(new long[]{200, 500})
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody.get("userName") + " хочет добавить вас в друзья"))
                        .setContentIntent(pendingIntent);
                int j = Integer.parseInt(messageBody.get("userId").replaceAll("[\\D]", "")) + 2;
                getManager().notify(j, notificationBuilder.build());
                try {
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), defaultSoundUri);
                    r.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case "cancelRequest": {
                int j = Integer.parseInt(messageBody.get("userId").replaceAll("[\\D]", "")) + 2;
                getManager().cancel(j);
                break;
            }
        }

    }

    public NotificationManager getManager(){
        if (notificationManager == null){
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

}