package com.dimovski.sportko.service;

import com.dimovski.sportko.data.Constants;
import com.dimovski.sportko.db.model.Event;
import com.dimovski.sportko.utils.NotificationUtils;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**Service that receives Firebase Cloud Messages
 * Called if the app is in foreground when the message is received, and handles it*/
public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static String TAG = "FBMS";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        remoteMessage.getNotification();
        Map data = remoteMessage.getData();
        Event e = new Event();
        String event = "";
        if (data!=null)
            event = (String) data.get(Constants.EVENT);

        NotificationUtils.sendNotification(remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody(),event);
    }

}
