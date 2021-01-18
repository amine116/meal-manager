package com.amine.mealmanager;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class MyNotificationChannels extends Application {
    public static final String ANNOUNCEMENT_ID = "ANNOUNCEMENT",
            PUB_MSG_ID = "PUB_MSG", NOTE_ID = "NOTE", MARKET_ID = "MARKET_LIST";

    @Override
    public void onCreate() {
        super.onCreate();
        createChannels();
    }

    private void createChannels(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channelAnnouncement = new NotificationChannel(
                    ANNOUNCEMENT_ID, "Announcement", NotificationManager.IMPORTANCE_HIGH);
            channelAnnouncement.setDescription("Announcement");

            NotificationChannel channelPubMsg = new NotificationChannel(
                    PUB_MSG_ID, "Public Message", NotificationManager.IMPORTANCE_HIGH);
            channelAnnouncement.setDescription("Public Message");


            NotificationChannel channelNote = new NotificationChannel(
                    NOTE_ID, "Note", NotificationManager.IMPORTANCE_HIGH);
            channelAnnouncement.setDescription("Notes");


            NotificationChannel channelMarket = new NotificationChannel(
                    MARKET_ID, "Market List", NotificationManager.IMPORTANCE_HIGH);
            channelAnnouncement.setDescription("Market List");



            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            notificationManager.createNotificationChannel(channelAnnouncement);
            notificationManager.createNotificationChannel(channelPubMsg);
            notificationManager.createNotificationChannel(channelNote);
            notificationManager.createNotificationChannel(channelMarket);
        }
    }

}
