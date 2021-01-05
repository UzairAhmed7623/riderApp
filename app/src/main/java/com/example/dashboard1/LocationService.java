package com.example.dashboard1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.dashboard1.Service.Globals;
import com.example.dashboard1.Service.ProcessMainClass;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class LocationService extends android.app.Service{
    protected static final String NOTIFICATION_ID = "1337";
    private static final String CHANNEL_ID = "channel_01";
    private static LocationService mCurrentService;
    private int counter = 0;
    private NotificationManager notificationManager;

    private static final String PACKAGE_NAME = "com.example.dashboard1";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME + ".started_from_notification";

    private LatLng latLng;
    private static final String TAG = LocationService.class.getSimpleName();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location_Helper location_helper;
    private LocationCallback locationCallback;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private ArrayList<LatLng> latLngs = new ArrayList<>();
    private float totalD = 0f;

    public LocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        locationCallback = new LocationCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onLocationResult(LocationResult locationResult) {

                Location location = locationResult.getLastLocation();

                if (location != null){

                    location_helper = new Location_Helper(location.getLongitude(), location.getLatitude());

                    Double Latitude = location_helper.getLatitude();
                    Double Longitude = location_helper.getLongitude();

                    latLng = new LatLng(Latitude, Longitude);

                    Long timeStamp = System.currentTimeMillis()/1000;

                    String time = getDate(timeStamp);

                    Log.d(TAG,"Latitude: "+Latitude+" Longitude: "+Longitude+" Time: "+time);

                    uploadFirebase(Latitude, Longitude);

//                String locationString = new StringBuilder("" + location.getLatitude()).append("/").append(location.getLongitude()).toString();

                    MainActivity.getInstance().showLocationTextView(Latitude, Longitude);

                    if (latLngs == null) {
                        latLngs = new ArrayList<LatLng>();
                    }
                    latLngs.add(new LatLng(Latitude, Longitude));

                    uploadHistory(latLngs, time);

                    calculateDistance(time);

                    if (serviceIsRunningInForeground(LocationService.this)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                            notificationManager.notify(1001, getNotification());
                        }
                        else {
                            notificationManager.notify(1002, getNotification());
                        }
                    }

//                Toast.makeText(getApplicationContext(), Latitude+" / "+Longitude +" / "+ time, Toast.LENGTH_SHORT).show();
                }
                else {
                    return;
                }

            }
        };


        mCurrentService = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "restarting Service !!");
        counter = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            startForeground(1001, getNotification());
        }
        else
            {
            startForeground(1002, getNotification());
        }

        // it has been killed by Android and now it is restarted. We must make sure to have reinitialised everything
        if (intent == null) {
            ProcessMainClass bck = new ProcessMainClass();
            bck.launchService(this);
        }

        // make sure you call the startForeground on onStartCommand because otherwise
        // when we hide the notification on onScreen it will nto restart in Android 6 and 7
        getLocationUpdates();
        startTimer();

        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,
                false);
        if (startedFromNotification) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            stopSelf();
        }

        return START_STICKY;
    }

    private void getLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(9000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,null);
    }

    private String getDate(Long timestamp){
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(timestamp * 1000);
        String date = DateFormat.format("dd-MM-yyyy", calendar).toString();
        return date;
    }

    public void uploadFirebase(double latitude, double longitude){
//        String[] Lat = String.valueOf(latitude).split("\\s,\\s");
//        String[] Lng = String.valueOf(longitude).split("\\s,\\s");

        LatLng latLng = new LatLng(latitude,longitude);

        Map<String, Object> user = new HashMap<>();
        user.put("Location", latLng);

        firebaseFirestore.collection("user").document(firebaseAuth.getUid())
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

    }

    private void uploadHistory(ArrayList<LatLng> latLngs, String time) {

        DocumentReference documentReference = firebaseFirestore.collection("user").document(firebaseAuth.getUid()).collection("location").document(time);
        Map<String, Object> location = new HashMap<>();
        location.put("History", latLngs);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot> () {
            @Override public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){
                            final LatLng latLng = latLngs.get(latLngs.size()-1);
                            documentReference.update("History", FieldValue.arrayUnion(latLng));
                            Log.w(TAG, "Document updated successfully!");

                    }
                    else {
                        firebaseFirestore.collection("user").document(firebaseAuth.getUid()).collection("location").document(time).set(location).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.w(TAG, "Document created Successfully");

                            }
                        });
                    }
                }

            }
        });
    }

    private void calculateDistance(String time) {


        DocumentReference documentReference = firebaseFirestore.collection("user").document(firebaseAuth.getUid()).collection("location").document(time);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){

                        List<Map<String, Double>> points = (List<Map<String, Double>>) document.get("History");

                        ArrayList<LatLng> latLngs = new ArrayList<>();

                        for (int x = 0; x < points.size(); x++){
                            latLngs.add(new LatLng(points.get(x).get("latitude"), points.get(x).get("longitude")));

//                            Log.d("TAG", latLngs.toString());
                        }


                        float tempTotalDistance = 0f;

                        for (int i =0; i < latLngs.size() -1; i++) {
                            LatLng pointA = latLngs.get(i);
                            LatLng pointB = latLngs.get(i+1);
//                            Log.d("ArrayPoints", "Point A" + String.valueOf(pointA.latitude+" "+pointA.longitude));
//                            Log.d("ArrayPoints", "PointB" + String.valueOf(pointB.latitude+" "+pointB.longitude));

//                                   LatLng pointA = new LatLng(points.get(i).get("latitude"), points.get(i).get("longitude"));
//                                   LatLng pointB = new LatLng(points.get(i+1).get("latitude"), points.get(i+1).get("longitude"));
                            float[] results = new float[1];
                            Location.distanceBetween (pointA.latitude, pointA.longitude, pointB.latitude, pointB.longitude, results);
                            tempTotalDistance +=  results[0];
//                            Log.d("Distance", "tempTotalDistance: "+String.valueOf(tempTotalDistance));
                        }
                        totalD = tempTotalDistance / 1000;
//                        Log.d("Distance", "totalD: "+String.valueOf(totalD));
                        float dis = Float.parseFloat(String.format("%.1f", totalD));
                        MainActivity.getInstance().calculateDistance(dis);
//                        Log.d("Distance", "dis: "+String.valueOf(dis));
                    }

                }
            }
        });


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    public Notification getNotification() {
        Intent intent = new Intent(this, LocationService.class);

        Log.i(TAG, "restarting foreground");

        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_ID)
                .setSmallIcon(R.drawable.ic_sleep)
                .setContentTitle("riderApp" + " " + "My Location Points")
                .setContentText("" + latLng)
                .setTicker("" + latLng)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(R.drawable.ic_launch, getString(R.string.launch_activity), activityPendingIntent)
                .addAction(R.drawable.ic_cancel, getString(R.string.remove_location_updates), servicePendingIntent);

        Log.i(TAG, "restarting foreground successful");

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
            startForeground(1001, builder.build());
        }
        else
        {
            startForeground(1002, builder.build());
        }

        return builder.build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "onDestroy called");
        // restart the never ending service
        Intent broadcastIntent = new Intent(Globals.RESTART_INTENT);
        sendBroadcast(broadcastIntent);
        stoptimertask();

        Log.d(TAG,"onDestroy: called");
//        stopForeground(true);

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i(TAG, "onTaskRemoved called");
        // restart the never ending service
        Intent broadcastIntent = new Intent(Globals.RESTART_INTENT);
        sendBroadcast(broadcastIntent);
        // do not call stoptimertask because on some phones it is called asynchronously
        // after you swipe out the app and therefore sometimes
        // it will stop the timer after it was restarted
        // stoptimertask();
    }

    /**
     * static to avoid multiple timers to be created when the service is called several times
     */
    private static Timer timer;
    private static TimerTask timerTask;
    long oldTime = 0;

    public void startTimer() {
        Log.i(TAG, "Starting timer");

        //set a new Timer - if one is already running, cancel it to avoid two running at the same time
        stoptimertask();
        timer = new Timer();
        //initialize the TimerTask's job
        initializeTimerTask();

        Log.i(TAG, "Scheduling...");
        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask() {
        Log.i(TAG, "initialising TimerTask");
        timerTask = new TimerTask() {
            public void run() {
                Log.i("in timer", "in timer ++++  " + (counter++));
            }
        };
    }

    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public static LocationService getmCurrentService() {
        return mCurrentService;
    }

    public static void setmCurrentService(LocationService mCurrentService) {
        LocationService.mCurrentService = mCurrentService;
    }

    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }
}
