package com.example.dashboard1.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.dashboard1.Common.Common;
import com.example.dashboard1.CustomerCall;
import com.example.dashboard1.EventBus.DriverRequestRecieved;
import com.example.dashboard1.LocationService;
import com.example.dashboard1.Models.TokenModel;
import com.example.dashboard1.R;
import com.example.dashboard1.Utils.UserUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;
import java.util.Random;

public class MyFirebaseService extends FirebaseMessagingService {

    private static final String FCM_CHANNEL_ID = "1001";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        UserUtils.updateToken(this, token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d("TAG", "Message recieved from: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0){
            Log.d("TAG", "Data received: " + remoteMessage.getData().toString());

            String title = remoteMessage.getData().get("title");
            String riderKey = remoteMessage.getData().get("RiderKey");
            String body = remoteMessage.getData().get("PickupLocation");

            Log.d("TAG", "Title: " + title + "Body: " + body);

            if (title.equals("RequestDriver")){
                Log.d("TAG", "Title: " + title);

                EventBus.getDefault().postSticky(new DriverRequestRecieved(riderKey, body));
                Log.d("TAG", "Chala");

            }
            else {
                Intent intent = new Intent(this, MyFirebaseService.class);
                Log.d("TAG", "Chala");

                Common.showNotification(this, new Random().nextInt(), title, body, intent);
            }


        }
    }
}