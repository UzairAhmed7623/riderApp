package com.example.dashboard1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Info;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.example.dashboard1.EventBus.DriverRequestRecieved;
import com.example.dashboard1.Utils.UserUtils;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class Orders extends AppCompatActivity implements OnMapReadyCallback {

    private static final String DIRECTION_API_KEY = "AIzaSyDl7YXtTZQNBkthV3PjFS0fQOKvL8SIR7k";
    private GoogleMap mgoogleMap;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private Chip chipDecline;
    private CardView layout_accept;
    private CircularProgressBar progress_circular_bar;
    private TextView tvRating, tvEstimatedTime, tvEstimatedDistance, tvTypeUber;

    //Routes
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Polyline blackPolyLine, greyPolyline;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private List<LatLng> polylineList;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    SupportMapFragment mapFragment;

    private boolean isFirstTime = true;

    private DatabaseReference onlineRef, currentUserRef, driversLocationRef;
    private GeoFire geoFire;
    private ValueEventListener onlineValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists() && currentUserRef != null) {
                currentUserRef.onDisconnect().removeValue();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onDestroy() {

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        geoFire.removeLocation(FirebaseAuth.getInstance().getCurrentUser().getUid());
        onlineRef.removeEventListener(onlineValueEventListener);

        if (EventBus.getDefault().hasSubscriberForEvent(DriverRequestRecieved.class)) {
            EventBus.getDefault().removeStickyEvent(DriverRequestRecieved.class);
        }
        EventBus.getDefault().unregister(this);

        compositeDisposable.clear();

        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerOnlineSystem();
    }

    private void registerOnlineSystem() {
        onlineRef.addValueEventListener(onlineValueEventListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        getSupportActionBar().hide();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        chipDecline = (Chip) findViewById(R.id.chipDecline);
        layout_accept = (CardView) findViewById(R.id.layout_accept);
        progress_circular_bar = (CircularProgressBar) findViewById(R.id.progress_circular_bar);
        tvRating = (TextView) findViewById(R.id.tvRating);
        tvEstimatedTime = (TextView) findViewById(R.id.tvEstimatedTime);
        tvEstimatedDistance = (TextView) findViewById(R.id.tvEstimatedDistance);
        tvTypeUber = (TextView) findViewById(R.id.tvTypeUber);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        init();

        if (firebaseAuth.getCurrentUser() != null) {
            updateFirebaseToken();
        }

    }

    private void updateFirebaseToken() {

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(Orders.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // Get new FCM registration token
                        String token = task.getResult();

                        UserUtils.updateToken(Orders.this, token);

                    }
                });

    }

    private void init() {
        onlineRef = FirebaseDatabase.getInstance().getReference().child("info/connected");

        locationRequest = LocationRequest.create();
        locationRequest.setSmallestDisplacement(20f);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(15000);
        locationRequest.setFastestInterval(10000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                LatLng newPosition = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                mgoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 18f));

                Location location = locationResult.getLastLocation();
                String id = FirebaseAuth.getInstance().getCurrentUser().getUid();

                Geocoder geocoder = new Geocoder(Orders.this, Locale.getDefault());
                List<Address> addressList;
                try {
                    addressList = geocoder.getFromLocation(location.getLatitude(),
                            location.getLongitude(), 1);
                    String cityName = addressList.get(0).getLocality();

                    driversLocationRef = FirebaseDatabase.getInstance().getReference("driversLocation").child(cityName);
                    currentUserRef = driversLocationRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    geoFire = new GeoFire(driversLocationRef);

                    geoFire.setLocation(id, new GeoLocation(location.getLatitude(), location.getLongitude()),
                            new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {
                                    if (error != null) {
                                        Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_LONG).show();
                                    }

                                }
                            });

                    registerOnlineSystem();

                } catch (IOException e) {
                    Snackbar.make(mapFragment.getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                }

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mgoogleMap = googleMap;

        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                if (ActivityCompat.checkSelfPermission(Orders.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Orders.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mgoogleMap.setMyLocationEnabled(true);
                mgoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
                mgoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public boolean onMyLocationButtonClick() {

                        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                mgoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 18f));
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Orders.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                        return true;
                    }
                });

                View locationbutton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent())
                        .findViewById(Integer.parseInt("2"));
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationbutton.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                params.setMargins(0, 0, 0, 50);
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                Toast.makeText(Orders.this, "Permission " + permissionDeniedResponse.getPermissionName() + " " +
                        "was denied!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();

        Snackbar.make(mapFragment.getView(), "You're online!", Snackbar.LENGTH_LONG).show();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDriverRequestReceived(DriverRequestRecieved event) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                try {

                    LatLng originLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    LatLng destinationLatLng = new LatLng(Double.parseDouble(event.getPickupLocation().split(",")[0]),
                            Double.parseDouble(event.getPickupLocation().split(",")[1]));

                    Log.d("address: ","Chala1");

                    Log.d("address: ",""+destinationLatLng);

                    GoogleDirection.withServerKey(DIRECTION_API_KEY)
                                .from(originLatLng)
                                .to(destinationLatLng)
                                .execute(new DirectionCallback() {
                                    @Override
                                    public void onDirectionSuccess(@Nullable Direction direction) {
                                        Route route = direction.getRouteList().get(0);
                                        Leg leg = route.getLegList().get(0);

                                        polylineList = leg.getDirectionPoint();


                                        polylineOptions = new PolylineOptions();
                                        polylineOptions.color(Color.GRAY);
                                        polylineOptions.width(12);
                                        polylineOptions.startCap(new SquareCap());
                                        polylineOptions.jointType(JointType.ROUND);
                                        polylineOptions.addAll(polylineList);
                                        greyPolyline = mgoogleMap.addPolyline(polylineOptions);

                                        blackPolylineOptions = new PolylineOptions();
                                        blackPolylineOptions.color(Color.BLACK);
                                        blackPolylineOptions.width(5);
                                        blackPolylineOptions.startCap(new SquareCap());
                                        blackPolylineOptions.jointType(JointType.ROUND);
                                        blackPolylineOptions.addAll(polylineList);
                                        greyPolyline = mgoogleMap.addPolyline(polylineOptions);
                                        blackPolyLine = mgoogleMap.addPolyline(blackPolylineOptions);

                                        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);
                                        valueAnimator.setDuration(1000);
                                        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
                                        valueAnimator.setInterpolator(new LinearInterpolator());
                                        valueAnimator.addUpdateListener(animation -> {
                                            List<LatLng> points = greyPolyline.getPoints();
                                            int percentValue = (int) animation.getAnimatedValue();
                                            int size = points.size();
                                            int newPoints = (int) (size*(percentValue/100.0f));
                                            List<LatLng> p = points.subList(0, newPoints);
                                            blackPolyLine.setPoints(p);
                                        });

                                        valueAnimator.start();

                                        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                                                .include(originLatLng)
                                                .include(destinationLatLng)
                                                .build();

                                        //Add car icon for origin

                                        Info distanceInfo = leg.getDistance();
                                        Info durationInfo = leg.getDuration();
                                        String distance = distanceInfo.getText();
                                        String duration = durationInfo.getText();

                                        tvEstimatedDistance.setText(distance);
                                        tvEstimatedTime.setText(duration);

                                        mgoogleMap.addMarker(new MarkerOptions()
                                        .position(destinationLatLng)
                                        .icon(BitmapDescriptorFactory.defaultMarker())
                                        .title("Pickup Location"));

                                        mgoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 160));
                                        mgoogleMap.moveCamera(CameraUpdateFactory.zoomTo(mgoogleMap.getCameraPosition().zoom - 1));

                                        chipDecline.setVisibility(View.VISIBLE);
                                        layout_accept.setVisibility(View.VISIBLE);

                                        Observable.interval(100, TimeUnit.MILLISECONDS)
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .doOnNext(x -> {
                                                    progress_circular_bar.setProgress(progress_circular_bar.getProgress()+1f);
                                                })
                                                .takeUntil(aLong -> aLong == 100)
                                                .doOnComplete(()->{
                                                    Toast.makeText(Orders.this, "Fake accept action", Toast.LENGTH_SHORT).show();
                                                }).subscribe();

                                    }

                                    @Override
                                    public void onDirectionFailure(@NonNull Throwable t) {
                                        Log.d("address: ","Chala2");

                                        Snackbar.make(mapFragment.getView(), t.getMessage(), Snackbar.LENGTH_LONG).show();
                                    }
                                });
                    }
                    catch (Exception e){
                        Snackbar.make(mapFragment.getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
                        Log.d("address: ","Chala3");

                    }

            }
        }).addOnFailureListener(e -> {
            Snackbar.make(mapFragment.getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
            Log.d("address: ","Chala4");

        });
    }
}