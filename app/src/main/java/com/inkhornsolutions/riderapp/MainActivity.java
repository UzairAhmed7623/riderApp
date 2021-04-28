package com.inkhornsolutions.riderapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private static final float MINIMUM_DISTANCE_BETWEEN_POINTS = 1; // If the user hasn't moved at least 10 meters, we will not take the location into account
    private static final String TAG = MainActivity.class.getSimpleName();
    static MainActivity instance;
    private static final int ALARM_ID = 1001;
    private TextView tvDis;
    private GoogleMap mgoogleMap;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private DrawerLayout drawerLayout;
    private final ArrayList<LatLng> latLngs = new ArrayList<>();
    boolean check = true;
    private Menu menu;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private Intent intent;
    private BottomSheetDialog bottomSheetDialog;
    private TextView tvUserName;
    private CircleImageView ivProfilePic;

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_drawer);

        instance = this;

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigation_View = findViewById(R.id.navigation_View);
        View header_View = navigation_View.getHeaderView(0);

        drawerLayout = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigation_View.setNavigationItemSelectedListener(this);

        tvUserName = (TextView) header_View.findViewById(R.id.tvUserName);
        headerTextView();

        ivProfilePic = (CircleImageView) header_View.findViewById(R.id.ivProfilePic);
        headerImage();

        tvDis = (TextView) findViewById(R.id.tvDis);

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragMap);
        supportMapFragment.getMapAsync(MainActivity.this);
    }

    public void headerTextView(){
        if (firebaseAuth != null) {
            DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()){
                            if (documentSnapshot.getString("firstName") != null && documentSnapshot.getString("lastName") != null){
                                String fuser_Name = documentSnapshot.getString("firstName");
                                String luser_Name = documentSnapshot.getString("lastName");
                                tvUserName.setText(fuser_Name +" "+luser_Name);
                            }
                            else {
                                Log.d("TAG", "No data found!");
                            }
                        }
                        else {
                            Toast.makeText(MainActivity.this, "No data found!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Log.d("TAG", task.getException().getMessage());
                    }
                }
            });
        }

    }

    public void headerImage(){
        if (firebaseAuth != null) {
            DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()){
                            if (documentSnapshot.getString("driverProfileImage") != null){
                                String imageUri = documentSnapshot.getString("driverProfileImage");
                                Glide.with(MainActivity.this).load(imageUri).into(ivProfilePic);
                            }
                            else {
                                Log.d("TAG", "Not found!");
                            }
                        }
                        else {
                            Log.d("TAG", "No data found!");
                        }
                    }
                    else {
                        Log.d("TAG", task.getException().getMessage());
                    }
                }
            });
        }

    }

    private void isGPSOn() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d(TAG, locationSettingsResponse.toString());
                if (check){
                    menu.getItem(1).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.location_on));
                    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    boolean providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    if (providerEnabled) {
                        startLocationService();
                    }
                    else {
                        isGPSOn();
                    }
                    check = false;
//                    Toast.makeText(instance, "check", Toast.LENGTH_SHORT).show();
                }
                else {
                    menu.getItem(1).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.location_off));
                    stopLocationService();
                    check = true;
//                    Toast.makeText(instance, "not", Toast.LENGTH_SHORT).show();
                }
            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                        resolvableApiException.startResolutionForResult(MainActivity.this, 1003);
                    } catch (IntentSender.SendIntentException sendEx) {
                        Log.d(TAG, "Error : " + sendEx);
                    }
                }
            }
        });
    }

    @SuppressLint("ShortAlarm")
    private void startLocationService() {

        alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        intent = new Intent(this, AlarmReceiver.class);
        intent.setAction("Background-Service");

        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), ALARM_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // schedule for every 10 seconds
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 0, 10 * 1000, pendingIntent);

        Toast.makeText(this, "Location updates started", Toast.LENGTH_SHORT).show();
    }

    private void stopLocationService() {

        if (pendingIntent == null){
            Toast.makeText(MainActivity.this, "Please start updates first and then press stop button!",Toast.LENGTH_LONG).show();
        }
        else {
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), ALARM_ID, intent, 0);
            pendingIntent.cancel();
            alarmManager.cancel(pendingIntent);

            Intent intent1 = new Intent(this, LocationService.class);
            stopService(intent1);
            Toast.makeText(this, "Location updates stopped", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mgoogleMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mgoogleMap.setMyLocationEnabled(true);
        mgoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
//
//        View locationButton = ((View) findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
//        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
//        // position on right bottom
//        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
//        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
//        rlp.setMargins(0, 1000, 1000, 0);

    }

    public void showLocationTextView(double lat, double lng) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                LatLng latLng = new LatLng(lat, lng);
                mgoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

                latLngs.add(latLng);

            }
        });
    }

    private void add_Location_Points(ArrayList<LatLng> latLngs, String time) {

        DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid()).collection("location").document(time);
        Map<String, Object> location_points = new HashMap<>();
        location_points.put("Location_Points", latLngs);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){
                        final LatLng latLng = latLngs.get(latLngs.size()-1);
                        documentReference.update("Location_Points", FieldValue.arrayUnion(latLng));
                        Log.w(TAG, "Document updated successfully!");

                    }
                    else {
                        firebaseFirestore.collection("Users").document(firebaseAuth.getUid()).collection("location").document(time).set(location_points).addOnCompleteListener(new OnCompleteListener<Void>() {
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

    private String getDate(Long timestamp){
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(timestamp * 1000);
        String date = DateFormat.format("dd-MM-yyyy", calendar).toString();
        return date;
    }

    public boolean distanceBetweenPoints(@NonNull Location location1, @NonNull Location location2) {
        return !(location1.distanceTo(location2) < MINIMUM_DISTANCE_BETWEEN_POINTS);
    }

    public void calculateDistance(float dis) {
        tvDis.setText(String.valueOf(dis));
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.profile:
                startActivity(new Intent(MainActivity.this, Profile.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;

            case R.id.orders:
                startActivity(new Intent(MainActivity.this, Orders.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;

            case R.id.history:
                startActivity(new Intent(MainActivity.this, History.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;

            case R.id.contactUs:
                View bottomSheetLayout1 = getLayoutInflater().inflate(R.layout.contact_us_dialog, null);
                (bottomSheetLayout1.findViewById(R.id.btnOk)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.dismiss();
                    }
                });

                (bottomSheetLayout1.findViewById(R.id.btnContact)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.dismiss();
                        Dexter.withContext(MainActivity.this).withPermission(Manifest.permission.CALL_PHONE).withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                String ph = "tel:" + "03133394722";
                                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(ph));
                                startActivity(intent);
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                                Toast.makeText(MainActivity.this, "Please accept the permission!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        }).check();
                    }
                });

                bottomSheetDialog = new BottomSheetDialog(this);
                bottomSheetDialog.setContentView(bottomSheetLayout1);
                bottomSheetDialog.show();
                bottomSheetDialog.setCancelable(false);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;

            case R.id.about:
                View bottomSheetLayout2 = getLayoutInflater().inflate(R.layout.about_dialog, null);
                (bottomSheetLayout2.findViewById(R.id.btnOk)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.dismiss();
                    }
                });

                bottomSheetDialog = new BottomSheetDialog(this);
                bottomSheetDialog.setContentView(bottomSheetLayout2);
                bottomSheetDialog.show();
                bottomSheetDialog.setCancelable(false);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;

            case R.id.signOut:
                SharedPreferences sharedPreferences = getSharedPreferences("checkBox", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("remember", "false");
                editor.apply();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;

            case R.id.nav_log_version:
                String url = "https://inkhornsolutions.com/\n";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.toolbarmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.addLocation:
                Long timeStamp = System.currentTimeMillis()/1000;
                String time = getDate(timeStamp);
                if (latLngs.size() >0){
                    add_Location_Points(latLngs, time);
                    Toast.makeText(MainActivity.this, "Location added successfully!", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(this, "Null location cannot be added!", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.startLocation:
                Dexter.withContext(MainActivity.this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        isGPSOn();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this, "Please accept the permission!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

                break;
        }

        return true;
    }
}
