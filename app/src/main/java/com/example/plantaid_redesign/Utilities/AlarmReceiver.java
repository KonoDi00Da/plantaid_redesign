package com.example.plantaid_redesign.Utilities;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.plantaid_redesign.LoginRegister.SplashScreen;
import com.example.plantaid_redesign.R;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "alarm";
    private static final String CHANNEL_ID = "SAMPLE_CHANNEL";

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onReceive(Context context, Intent intent) {
        //Get id and message from intent
        try{

            int notificationID = intent.getIntExtra("notificationID", 0);
            int requestCode = intent.getIntExtra("requestCode", 0);
            String title = intent.getStringExtra("title");
            String plantName = intent.getStringExtra("plantName");
            String message = intent.getStringExtra("task");
            if(message.equals("")){
                message = "No content";
            }else if(message.equals("Water")){
                message = "Your plant needs watering!";
            }else if(message.equals("Fertilize")){
                message = "Your plant needs fertilizing!";
            }else if(message.equals("Repot")) {
                message = "Your plant needs repotting!";
            }else{
                message = "You have a task! " + message;
            }
            String del = intent.getStringExtra("delete");

            //When notification is tapped call main activity
            Intent main = new Intent(context, SplashScreen.class);
            main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);


            PendingIntent contentIntent = PendingIntent.getActivity(context,
                    requestCode,
                    main,
                    PendingIntent.FLAG_IMMUTABLE);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            //Prepare notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSmallIcon(R.mipmap.plantaid_logo_round)
                    .setContentTitle(plantName)
                    .setContentText(message)
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setDefaults(Notification.DEFAULT_ALL);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                //for api 26 and above
                CharSequence channel_name = "My Notification";
                int importance = NotificationManager.IMPORTANCE_HIGH;

                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channel_name, importance);
                notificationManager.createNotificationChannel(channel);
                builder.setChannelId(CHANNEL_ID);
            }
            //notify

            notificationManager.notify(notificationID, builder.build());
        }catch(Exception e){
            Log.e(TAG,"error",e);
        }

    }
}
