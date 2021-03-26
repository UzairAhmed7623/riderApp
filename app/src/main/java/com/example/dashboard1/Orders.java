package com.example.dashboard1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Orders extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mgoogleMap;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private boolean isConnected = false;
    private TextView cusName, cusPhone;
    private LinearLayout layout;
    private ImageView cusImage;
    private List<String> keys = new ArrayList<>();
    private String key = "";
    private DocumentReference documentReference;
    private CollectionReference ref;
    private Marker marker, cusMarker;
    private ProgressDialog progressDialog;
    private Location location;

    private DatabaseReference driversRef;
    private Marker currentMarker;
    private Button btnFindUsers;
    //car animation
    private List<LatLng> polylineList;
    private LatLng currentPosition;

    private String destination;


    //new wale sare is k neche hn.
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    SupportMapFragment mapFragment;


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

        super.onDestroy();
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

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        String id = firebaseAuth.getCurrentUser().getUid();

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

        registerOnlineSystem();

        locationRequest = LocationRequest.create();
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(9000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                LatLng newPosition = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                mgoogleMap.moveCamera(CameraUpdateFactory.newLatLng(newPosition));

                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                        new GeoLocation(locationResult.getLastLocation().getLatitude(),
                                locationResult.getLastLocation().getLongitude()),
                        new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                if (error != null){
                                    Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                                else {
                                    Snackbar.make(mapFragment.getView(), "You're online!", Snackbar.LENGTH_LONG).show();
                                }
                            }
                        });

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                LatLng newPosition = new LatLng(location.getLatitude(), location.getLongitude());
                mgoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 18f));

                Geocoder geocoder = new Geocoder(Orders.this, Locale.getDefault());
                List<Address> addressList;
                try {
                    addressList = geocoder.getFromLocation(location.getLatitude(),
                            location.getLongitude(), 1);
                    String cityName = addressList.get(0).getLocality();

                    driversLocationRef = FirebaseDatabase.getInstance().getReference("driversLocation").child(cityName);
                    currentUserRef = driversLocationRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    geoFire = new GeoFire(driversLocationRef);


                } catch (IOException e) {
                    Snackbar.make(mapFragment.getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(mapFragment.getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
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
                                            Toast.makeText(Orders.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            return true;
                        }
                    });

                    View locationbutton = ((View)mapFragment.getView().findViewById(Integer.parseInt("1")).getParent())
                            .findViewById(Integer.parseInt("2"));
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationbutton.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                    params.setMargins(0,0,0,50);
                }

                @Override
                public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                    Toast.makeText(Orders.this, "Permission "+permissionDeniedResponse.getPermissionName()+" "+
                            "was denied!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                    permissionToken.continuePermissionRequest();
                }
            }).check();

    }

//    private void getDirection() {
//
////        currentPosition = new LatLng(Common.latLng1.latitude, Common.latLng1.longitude);
//        Toast.makeText(this, ""+currentPosition, Toast.LENGTH_SHORT).show();
//
//        String requestApi = null;
//        try {
//            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
//                    "mode=driving&" +
//                    "transit_routing_preference=less_driving&" +
//                    "origin=" + currentPosition.latitude + "," + currentPosition.longitude + "&" +
//                    "destination=" + destination + "&" +
//                    "key=" + "AIzaSyDl7YXtTZQNBkthV3PjFS0fQOKvL8SIR7k";
//
//            Log.d("TAG", requestApi);
//
//            mService.getPath(requestApi).enqueue(new Callback<String>() {
//                @Override
//                public void onResponse(Call<String> call, Response<String> response) {
//                    try {
//                        JSONObject jsonObject = new JSONObject(response.body().toString());
//                        JSONArray jsonArray = jsonObject.getJSONArray("routes");
//                        for (int i=0; i<jsonArray.length(); i++){
//                            JSONObject route = jsonArray.getJSONObject(i);
//                            JSONObject poly = route.getJSONObject("overview_polyline");
//                            String polyline = poly.getString("points");
//                            polylineList = decodePoly(polyline);
//
//                            Log.d("TAG", "" + polylineList);
//
//                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
//                            for (LatLng latLng : polylineList) {
//                                builder.include(latLng);
//                                LatLngBounds bounds = builder.build();
//                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
//                                mgoogleMap.animateCamera(cameraUpdate);
//
//
//                                mgoogleMap.addMarker(new MarkerOptions().position(polylineList.get(polylineList.size() - 1)).title("Pickup Location"));
//
//                            }
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<String> call, Throwable t) {
//                    Toast.makeText(Orders.this, t.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    private List decodePoly(String encoded) {
//
//        List poly = new ArrayList();
//        int index = 0, len = encoded.length();
//        int lat = 0, lng = 0;
//
//        while (index < len) {
//            int b, shift = 0, result = 0;
//            do {
//                b = encoded.charAt(index++) - 63;
//                result |= (b & 0x1f) << shift;
//                shift += 5;
//            } while (b >= 0x20);
//            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
//            lat += dlat;
//
//            shift = 0;
//            result = 0;
//            do {
//                b = encoded.charAt(index++) - 63;
//                result |= (b & 0x1f) << shift;
//                shift += 5;
//            } while (b >= 0x20);
//            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
//            lng += dlng;
//
//            LatLng p = new LatLng((((double) lat / 1E5)),
//                    (((double) lng / 1E5)));
//            poly.add(p);
//        }
//
//        return poly;
//    }

}