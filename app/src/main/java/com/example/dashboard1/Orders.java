package com.example.dashboard1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Info;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.example.dashboard1.Common.Common;
import com.example.dashboard1.EventBus.DriverRequestRecieved;
import com.example.dashboard1.EventBus.NotifyToRiderEvent;
import com.example.dashboard1.Models.DriverInfoModel;
import com.example.dashboard1.Models.RiderModel;
import com.example.dashboard1.Models.TripPlanModel;
import com.example.dashboard1.Utils.UserUtils;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firestore.v1.StructuredQuery;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.kusu.loadingbutton.LoadingButton;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class Orders extends AppCompatActivity implements OnMapReadyCallback {

    private static final String DIRECTION_API_KEY = "AIzaSyDl7YXtTZQNBkthV3PjFS0fQOKvL8SIR7k";
    private GoogleMap mgoogleMap;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private Chip chipDecline;
    private CardView layout_accept;
    private FrameLayout rootLayout;
    private CircularProgressBar progress_circular_bar;
    private TextView tvRating, tvEstimatedTime, tvEstimatedDistance, tvTypeUber;
    private ImageView ivRound;

    private CardView layout_start_ride;
    private ImageView ivStartRide, ivPhoneCall, ivThreeDot;
    private TextView tvStartRiderEstimateTime, tvStartRiderEstimateDistance, tvRiderName;
    private LoadingButton btnStartRide;
    private LoadingButton btnCompleteRide;

    private LinearLayout layout_notify_rider;
    private TextView tvNotifyRider;
    private ProgressBar progressNotify;
    //Routes
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Polyline blackPolyLine, greyPolyline;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private List<LatLng> polylineList;

    private DriverRequestRecieved driverRequestReceived;
    private Disposable countDownEvent;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    SupportMapFragment mapFragment;

    private boolean isFirstTime = true;
    private String id = FirebaseAuth.getInstance().getCurrentUser().getUid();

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

    private String tripNumberId = "";
    private boolean isTripStart = false, onlineSystemAlreadyRegister = false;

    private GeoFire pickupGeoFire, destinationGeoFire;
    private GeoQuery pickupGeoQuery, destinationGeoQuery;

    private GeoQueryEventListener pickupGeoQueryEventListner = new GeoQueryEventListener() {
        @Override
        public void onKeyEntered(String key, GeoLocation location) {
            btnStartRide.setEnabled(true);
            UserUtils.sendNotifyToRider(Orders.this, rootLayout, key);
            if (pickupGeoQuery != null) {
                pickupGeoFire.removeLocation(key);
                pickupGeoFire = null;
                pickupGeoQuery.removeAllListeners();
            }
        }

        @Override
        public void onKeyExited(String key) {
            btnStartRide.setEnabled(false);
        }

        @Override
        public void onKeyMoved(String key, GeoLocation location) {

        }

        @Override
        public void onGeoQueryReady() {

        }

        @Override
        public void onGeoQueryError(DatabaseError error) {

        }
    };
    private GeoQueryEventListener destinationGeoQueryEventListner = new GeoQueryEventListener() {
        @Override
        public void onKeyEntered(String key, GeoLocation location) {
            btnCompleteRide.setEnabled(true);
            if (destinationGeoQuery != null) {
                destinationGeoFire.removeLocation(key);
                destinationGeoFire = null;
                destinationGeoQuery.removeAllListeners();
            }
        }

        @Override
        public void onKeyExited(String key) {

        }

        @Override
        public void onKeyMoved(String key, GeoLocation location) {

        }

        @Override
        public void onGeoQueryReady() {

        }

        @Override
        public void onGeoQueryError(DatabaseError error) {

        }
    };

    private CountDownTimer waitingTimer;

    @Override
    protected void onDestroy() {

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        geoFire.removeLocation(FirebaseAuth.getInstance().getCurrentUser().getUid());
        onlineRef.removeEventListener(onlineValueEventListener);

        if (EventBus.getDefault().hasSubscriberForEvent(DriverRequestRecieved.class)) {
            EventBus.getDefault().removeStickyEvent(DriverRequestRecieved.class);
        }
        if (EventBus.getDefault().hasSubscriberForEvent(NotifyToRiderEvent.class)) {
            EventBus.getDefault().removeStickyEvent(NotifyToRiderEvent.class);
        }
        EventBus.getDefault().unregister(this);

        compositeDisposable.clear();

        onlineSystemAlreadyRegister = false;

        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {

            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerOnlineSystem();
    }

    private void registerOnlineSystem() {
        if (!onlineSystemAlreadyRegister) {
            onlineRef.addValueEventListener(onlineValueEventListener);
            onlineSystemAlreadyRegister = true;
        }
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
        rootLayout = (FrameLayout) findViewById(R.id.rootLayout);
        ivRound = (ImageView) findViewById(R.id.ivRound);
        layout_start_ride = (CardView) findViewById(R.id.layout_start_ride);
        ivStartRide = (ImageView) findViewById(R.id.ivStartRide);
        ivPhoneCall = (ImageView) findViewById(R.id.ivPhoneCall);
        ivThreeDot = (ImageView) findViewById(R.id.ivThreeDot);
        tvStartRiderEstimateTime = (TextView) findViewById(R.id.tvStartRiderEstimateTime);
        tvStartRiderEstimateDistance = (TextView) findViewById(R.id.tvStartRiderEstimateDistance);
        tvRiderName = (TextView) findViewById(R.id.tvRiderName);
        btnStartRide = (LoadingButton) findViewById(R.id.btnStartRide);
        layout_notify_rider = (LinearLayout) findViewById(R.id.layout_notify_rider);
        tvNotifyRider = (TextView) findViewById(R.id.tvNotifyRider);
        progressNotify = (ProgressBar) findViewById(R.id.progressNotify);
        btnCompleteRide = (LoadingButton) findViewById(R.id.btnCompleteRide);


        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        init();

        if (firebaseAuth.getCurrentUser() != null) {
            updateFirebaseToken();
        }

        chipDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (driverRequestReceived != null) {
                    if (TextUtils.isEmpty(tripNumberId)) {
                        if (countDownEvent != null) {
                            countDownEvent.dispose();
                        }
                        chipDecline.setVisibility(View.GONE);
                        layout_accept.setVisibility(View.GONE);
                        mgoogleMap.clear();
                        UserUtils.sendDeclineRequest(rootLayout, Orders.this, driverRequestReceived.getKey());
                        driverRequestReceived = null;

                    } else {
                        if (ActivityCompat.checkSelfPermission(Orders.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Orders.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {

                            chipDecline.setVisibility(View.GONE);
                            layout_start_ride.setVisibility(View.GONE);
                            mgoogleMap.clear();
                            UserUtils.sendDeclineAndRemoveRiderRequest(rootLayout, Orders.this,
                                    driverRequestReceived.getKey(), tripNumberId);

                            tripNumberId = "";
                            driverRequestReceived = null;

                            makeDriverOnline(location);

                        }).addOnFailureListener(e -> Snackbar.make(rootLayout, e.getMessage(), Snackbar.LENGTH_LONG).show());
                    }
                }
            }
        });

        btnStartRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (blackPolyLine != null) blackPolyLine.remove();
                if (greyPolyline != null) greyPolyline.remove();
                if (waitingTimer != null) waitingTimer.cancel();

                layout_notify_rider.setVisibility(View.GONE);
                if (driverRequestReceived != null) {
                    LatLng destinationLatLng = new LatLng(
                            Double.parseDouble(driverRequestReceived.getDestinationLocation().split(",")[0]),
                            Double.parseDouble(driverRequestReceived.getDestinationLocation().split(",")[1]));

                    mgoogleMap.addMarker(new MarkerOptions()
                            .position(destinationLatLng)
                            .title(driverRequestReceived.getDestinationLocationString())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

                    //draw path
                    drawPathFromCurrentLocation(driverRequestReceived.getDestinationLocation());
                }
                btnStartRide.setVisibility(View.GONE);
                chipDecline.setVisibility(View.GONE);
                btnCompleteRide.setVisibility(View.VISIBLE);
            }
        });

        btnCompleteRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> updateTrip = new HashMap<>();
                updateTrip.put("done", true);

                FirebaseDatabase.getInstance()
                        .getReference("Trips")
                        .child(tripNumberId)
                        .updateChildren(updateTrip).addOnSuccessListener(aVoid -> {

                    if (ActivityCompat.checkSelfPermission(Orders.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Orders.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            UserUtils.sendCompleteTripToRider(rootLayout, Orders.this, driverRequestReceived.getKey(), tripNumberId);

                            mgoogleMap.clear();
                            tripNumberId = "";
                            chipDecline.setVisibility(View.GONE);
                            layout_accept.setVisibility(View.GONE);
                            progress_circular_bar.setProgress(0);
                            layout_start_ride.setVisibility(View.GONE);
                            layout_notify_rider.setVisibility(View.GONE);
                            progressNotify.setProgress(0);
                            btnCompleteRide.setEnabled(false);
                            btnCompleteRide.setVisibility(View.GONE);
                            btnStartRide.setEnabled(false);
                            btnStartRide.setVisibility(View.GONE);
                            destinationGeoFire = null;
                            pickupGeoFire = null;
                            driverRequestReceived = null;

                            makeDriverOnline(location);
                        }
                    });

                        }).addOnFailureListener(e -> {
                    Snackbar.make(rootLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
            }
        });

    }

    private void drawPathFromCurrentLocation(String destinationLocation) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                try {

                    LatLng originLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    LatLng destinationLatLng = new LatLng(
                            Double.parseDouble(destinationLocation.split(",")[0]),
                            Double.parseDouble(destinationLocation.split(",")[1]));

                    Log.d("address: ", "Chala1");

                    Log.d("address: ", "" + destinationLatLng);

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



                                    LatLngBounds latLngBounds = new LatLngBounds.Builder()
                                            .include(originLatLng)
                                            .include(destinationLatLng)
                                            .build();

                                    createGeoFireDestinationLocation(driverRequestReceived.getKey(), destinationLatLng);

                                    mgoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 160));
                                    mgoogleMap.moveCamera(CameraUpdateFactory.zoomTo(mgoogleMap.getCameraPosition().zoom - 1));

                                }

                                @Override
                                public void onDirectionFailure(@NonNull Throwable t) {
                                    Log.d("address: ", "Chala2");

                                    Snackbar.make(rootLayout, t.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            });
                } catch (Exception e) {
                    Snackbar.make(rootLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
                    Log.d("address: ", "Chala3");

                }

            }
        }).addOnFailureListener(e -> {
            Snackbar.make(rootLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
            Log.d("address: ", "Chala4");

        });
    }

    private void createGeoFireDestinationLocation(String key, LatLng destinationLatLng) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TripDestinationLocation");
        destinationGeoFire = new GeoFire(ref);
        destinationGeoFire.setLocation(key, new GeoLocation(destinationLatLng.latitude, destinationLatLng.longitude), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
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

        buildLocationRequest();
        buildLocationCallBack();
        updateLocation();

    }

    private void updateLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

    }

    private void buildLocationCallBack() {
        if (locationCallback == null) {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    super.onLocationResult(locationResult);

                    LatLng newPosition = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                    mgoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 18f));

                    Location location = locationResult.getLastLocation();

                    if (pickupGeoFire != null){
                        pickupGeoQuery = pickupGeoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 0.05);

                        pickupGeoQuery.addGeoQueryEventListener(pickupGeoQueryEventListner);
                    }

                    if (destinationGeoFire != null){
                        destinationGeoQuery = destinationGeoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 0.05);

                        destinationGeoQuery.addGeoQueryEventListener(destinationGeoQueryEventListner);
                    }

                    if (!isTripStart){
                        makeDriverOnline(locationResult.getLastLocation());
                    }
                    else {
                        if (!TextUtils.isEmpty(tripNumberId)){
                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("currentLat", locationResult.getLastLocation().getLatitude());
                            updateData.put("currentLng", locationResult.getLastLocation().getLongitude());

                            FirebaseDatabase.getInstance().getReference("Trips")
                                    .child(tripNumberId)
                                    .updateChildren(updateData)
                                    .addOnSuccessListener(aVoid -> {

                                    }).addOnFailureListener(e -> Snackbar.make(mapFragment.getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show());
                        }
                    }
                }
            };
        }
    }

    private void makeDriverOnline(Location location) {
        Geocoder geocoder = new Geocoder(Orders.this, Locale.getDefault());
        List<Address> addressList;
        try {
            addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
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

    private void buildLocationRequest() {
        if (locationRequest == null) {
            locationRequest = LocationRequest.create();
            locationRequest.setSmallestDisplacement(20f);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(15000);
            locationRequest.setFastestInterval(10000);
        }
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
                    @Override
                    public boolean onMyLocationButtonClick() {

                        if (ActivityCompat.checkSelfPermission(Orders.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Orders.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return false;
                        }
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

                buildLocationRequest();
                buildLocationCallBack();
                updateLocation();

                Snackbar.make(mapFragment.getView(), "You're online!", Snackbar.LENGTH_LONG).show();

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

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDriverRequestReceived(DriverRequestRecieved event) {

        driverRequestReceived = event;

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

                    Log.d("address: ", "Chala1");

                    Log.d("address: ", "" + destinationLatLng);

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
                                        int newPoints = (int) (size * (percentValue / 100.0f));
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
                                    
                                    createGeoFirePickupLocation(event.getKey(), destinationLatLng);

                                    mgoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 160));
                                    mgoogleMap.moveCamera(CameraUpdateFactory.zoomTo(mgoogleMap.getCameraPosition().zoom - 1));

                                    chipDecline.setVisibility(View.VISIBLE);
                                    layout_accept.setVisibility(View.VISIBLE);

                                    countDownEvent = Observable.interval(100, TimeUnit.MILLISECONDS)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .doOnNext(x -> {
                                                progress_circular_bar.setProgress(progress_circular_bar.getProgress() + 1f);
                                            })
                                            .takeUntil(aLong -> aLong == 100)
                                            .doOnComplete(() -> {

                                                createTripPlan(event, duration, distance);

                                            }).subscribe();

                                }

                                @Override
                                public void onDirectionFailure(@NonNull Throwable t) {
                                    Log.d("address: ", "Chala2");

                                    Snackbar.make(mapFragment.getView(), t.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            });
                } catch (Exception e) {
                    Snackbar.make(mapFragment.getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
                    Log.d("address: ", "Chala3");

                }

            }
        }).addOnFailureListener(e -> {
            Snackbar.make(mapFragment.getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
            Log.d("address: ", "Chala4");

        });
    }

    private void createGeoFirePickupLocation(String key, LatLng destinationLatLng) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TripPickupLocation");

        pickupGeoFire = new GeoFire(ref);
        pickupGeoFire.setLocation(key, new GeoLocation(destinationLatLng.latitude, destinationLatLng.longitude),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if (error != null){
                            Snackbar.make(rootLayout, error.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                        else {
                            Log.d("Success", key+"was create success on geofire");
                        }
                    }
                });

    }

    private void createTripPlan(DriverRequestRecieved event, String duration, String distance) {
        setProcessLayout(true);

        FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long timeOffset = snapshot.getValue(Long.class);

                firebaseFirestore.collection("Users").document(event.getKey())
                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                        if (snapshot.exists()) {
                            RiderModel riderModel = snapshot.toObject(RiderModel.class);

                            if (ActivityCompat.checkSelfPermission(Orders.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Orders.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            fusedLocationProviderClient.getLastLocation()
                                    .addOnSuccessListener(location -> {

                                        TripPlanModel tripPlanModel = new TripPlanModel();
                                        tripPlanModel.setDriver(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        tripPlanModel.setRider(event.getKey());

                                        firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid())
                                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                DriverInfoModel driverInfoModel = documentSnapshot.toObject(DriverInfoModel.class);


                                                tripPlanModel.setDriverInfoModel(driverInfoModel);
                                                tripPlanModel.setRiderModel(riderModel);
                                                tripPlanModel.setOrigin(event.getPickupLocation());
                                                tripPlanModel.setOriginString(event.getPickupLocationString());
                                                tripPlanModel.setDestination(event.getDestinationLocation());
                                                tripPlanModel.setDestinationString(event.getDestinationLocationString());
                                                tripPlanModel.setDistancePickup(distance);
                                                tripPlanModel.setDurationPickup(duration);
                                                tripPlanModel.setCurrentLat(location.getLatitude());
                                                tripPlanModel.setCurrentLng(location.getLongitude());

                                                tripNumberId = Common.createUniqueTripIdNumber(timeOffset);

                                                FirebaseDatabase.getInstance().getReference("Trips")
                                                        .child(tripNumberId)
                                                        .setValue(tripPlanModel)
                                                        .addOnSuccessListener(aVoid -> {

                                                            tvRiderName.setText(riderModel.getFirstName());
                                                            tvStartRiderEstimateTime.setText(duration);
                                                            tvStartRiderEstimateDistance.setText(distance);

                                                            setOfflineModeForDriver(event, duration, distance);

                                                        }).addOnFailureListener(e -> {
                                                    Snackbar.make(mapFragment.getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Snackbar.make(mapFragment.getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
                                            }
                                        });

                                    }).addOnFailureListener(e -> Snackbar.make(mapFragment.getView(), e.getMessage(), Snackbar.LENGTH_LONG).show());
                        }
                        else {
                            Snackbar.make(mapFragment.getView(), "Cannot find rider with key"+" "+event.getKey(), Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void setOfflineModeForDriver(DriverRequestRecieved event, String duration, String distance) {

        UserUtils.sendAcceptRequestToRider(mapFragment.getView(), this, event.getKey(), tripNumberId);

        if (currentUserRef != null){
            currentUserRef.removeValue();
        }
        setProcessLayout(false);
        layout_accept.setVisibility(View.GONE);

        layout_start_ride.setVisibility(View.VISIBLE);

        isTripStart = true;
    }

    private void setProcessLayout(boolean isProcess) {
        int color = -1;
        if (isProcess) {
            color = ContextCompat.getColor(this, R.color.dark_grey);
            progress_circular_bar.setIndeterminateMode(true);
            tvRating.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.star, 0);

        }
        else {
            color = ContextCompat.getColor(this, android.R.color.white);
            progress_circular_bar.setIndeterminateMode(false);
            progress_circular_bar.setProgress(0);
            tvRating.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.star, 0);

        }

            tvEstimatedTime.setTextColor(color);
            tvEstimatedDistance.setTextColor(color);
            ImageViewCompat.setImageTintList(ivRound, ColorStateList.valueOf(color));
            tvRating.setTextColor(color);
            tvTypeUber.setTextColor(color);

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onNotifytoRider(NotifyToRiderEvent event){
        layout_notify_rider.setVisibility(View.VISIBLE);
        progressNotify.setMax(1 * 60);
        waitingTimer = new CountDownTimer(1*60*1000, 1000) {
            @Override
            public void onTick(long l) {
                progressNotify.setProgress(progressNotify.getProgress() + 1);
                tvNotifyRider.setText(String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(l) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(l)),
                        TimeUnit.MILLISECONDS.toSeconds(l) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l))));
            }

            @Override
            public void onFinish() {
                Snackbar.make(rootLayout, "Time Over", Snackbar.LENGTH_LONG).show();
            }
        }.start();
    }
}