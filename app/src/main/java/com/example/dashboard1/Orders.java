package com.example.dashboard1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.koalap.geofirestore.GeoFire;
import com.koalap.geofirestore.GeoLocation;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Orders extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mgoogleMap;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private boolean isConnected = false;
    private TextView cusName, cusPhone;
    private LinearLayout layout;
    private ImageView cusImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        cusName = (TextView) findViewById(R.id.cusName) ;
        cusPhone = (TextView) findViewById(R.id.cusPhone) ;
        cusImage = (ImageView) findViewById(R.id.cusImage) ;
        layout = (LinearLayout) findViewById(R.id.layout) ;

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        String id = firebaseAuth.getCurrentUser().getUid();

        DocumentReference documentReference = firebaseFirestore.collection("Users").document(id);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (documentSnapshot.exists()){
                    String requestID = documentSnapshot.getString("requestID");
                    if (requestID != null){
                        isConnected = true;
                    }
                }
            }
        });

        getLocationUpdates();

    }

    private void getLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(9000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, getLocationCallback(),null);
    }

    private LocationCallback getLocationCallback() {

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location location = locationResult.getLastLocation();
                if (location != null){
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();


                    LatLng latLng = new LatLng(lat, lng);

                    Log.d("Location", "Location Points: " + latLng.latitude+" "+latLng.longitude);

                    String id = firebaseAuth.getCurrentUser().getUid();

                    firebaseFirestore.collection("Users").document(id).update("status", "available");

                    CollectionReference ref = firebaseFirestore.collection("Users");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(id, new GeoLocation(lat, lng));


                    if (isConnected){
                        getCustomerDetails(id, latLng);
                    }


                }
                else {
                    Toast.makeText(Orders.this, "Null", Toast.LENGTH_SHORT).show();
                }

            }
        };
        return locationCallback;
    }

    private void getCustomerDetails(String id, LatLng latLng) {

        layout.setVisibility(View.VISIBLE);
        DocumentReference documentReference = firebaseFirestore.collection("Users").document(id);

        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (documentSnapshot.exists()){
                    String customerId = documentSnapshot.getString("requestID");
                    DocumentReference documentReference = firebaseFirestore.collection("Users").document(customerId);
                    documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                            if (documentSnapshot.exists()){
                                String fName = documentSnapshot.getString("First Name");
                                String lName = documentSnapshot.getString("Last Name");
                                String phone = documentSnapshot.getString("Phone");
                                String imageUri = documentSnapshot.getString("imageProfile");
                                GeoPoint geoPoint = documentSnapshot.getGeoPoint("l");



                                cusName.setText(fName + " " + lName);
                                cusPhone.setText(phone);
                                Glide.with(Orders.this).load(imageUri).placeholder(getDrawable(R.drawable.driver_placeholder)).fitCenter().into(cusImage);

                                LatLng latLng1 = new LatLng(geoPoint.getLatitude(),geoPoint.getLongitude());
                                mgoogleMap.addMarker(new MarkerOptions().position(latLng1).title(fName + " " + lName));
//                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng1));
                                LatLngBounds.Builder latlngBuilder = new LatLngBounds.Builder();

                                latlngBuilder.include(latLng);
                                latlngBuilder.include(latLng1);

                                mgoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latlngBuilder.build(), 100));

                                GoogleDirection.withServerKey("AIzaSyDl7YXtTZQNBkthV3PjFS0fQOKvL8SIR7k")
                                        .from(latLng)
                                        .to(latLng1)
                                        .execute(new DirectionCallback() {
                                            @Override
                                            public void onDirectionSuccess(@Nullable Direction direction) {

                                                List<Step> stepList = direction.getRouteList().get(0).getLegList().get(0).getStepList();
                                                ArrayList<PolylineOptions> polylineOptionList = DirectionConverter.createTransitPolyline(Orders.this, stepList, 5, Color.RED, 3, Color.BLUE);
                                                for (PolylineOptions polylineOption : polylineOptionList) {
                                                    mgoogleMap.addPolyline(polylineOption);
                                                }
                                            }

                                            @Override
                                            public void onDirectionFailure(Throwable t) {
                                                Log.d("TAG222", ""+t.getMessage());

                                            }
                                        });

                            }
                        }
                    });
                }
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mgoogleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mgoogleMap.setMyLocationEnabled(true);
    }

    @Override
    protected void onStop() {
        super.onStop();

        String id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore.collection("Users").document(id).update("status", "not_available");
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        String id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore.collection("Users").document(id).update("status", "not_available");
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

    }
}