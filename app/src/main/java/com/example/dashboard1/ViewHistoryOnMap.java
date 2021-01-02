package com.example.dashboard1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ViewHistoryOnMap extends AppCompatActivity implements OnMapReadyCallback{

    private GoogleMap mgoogleMap;
    private Marker marker;
    private Polyline polyline;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private DocumentReference documentReference;
    private RecyclerView rvLocationView;
    private TextView tvDis;
    private float totalD = 0f;
    List<String> addr = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_history_on_map);

        tvDis = (TextView)findViewById(R.id.tvDis);

        rvLocationView = (RecyclerView) findViewById(R.id.rvLocationView);
        rvLocationView.setLayoutManager(new LinearLayoutManager(this));

        getSupportActionBar().hide();

        String doc = getIntent().getExtras().getString("doc");
        Toast.makeText(getApplicationContext(), doc, Toast.LENGTH_SHORT).show();

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragMap);
        supportMapFragment.getMapAsync(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        documentReference = firebaseFirestore.collection("user").document(firebaseAuth.getUid()).collection("location").document(doc);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()){
                        List<Map<String,Double>> flatLngs = (List<Map<String, Double>>) documentSnapshot.get("History");

                        ArrayList<LatLng> latLngs = new ArrayList<>();

                        for (int x = 0; x < flatLngs.size(); x++){
                            latLngs.add(new LatLng(flatLngs.get(x).get("latitude"), flatLngs.get(x).get("longitude")));
                        }

                        PolylineOptions polylineOptions = new PolylineOptions().addAll(latLngs).clickable(true);
                        polyline = mgoogleMap.addPolyline(polylineOptions);
                        polyline.setColor(Color.BLUE);
                        polyline.setWidth(10);

                        LatLng initialLatLng = new LatLng(latLngs.get(0).latitude,latLngs.get(0).longitude);
                        LatLng LastLatLng = new LatLng(latLngs.get(latLngs.size()-1).latitude,latLngs.get(latLngs.size()-1).longitude);
                        marker = mgoogleMap.addMarker(new MarkerOptions().position(initialLatLng));
                        marker = mgoogleMap.addMarker(new MarkerOptions().position(LastLatLng));

                        getDistance(latLngs);

                        double lat = latLngs.get(0).latitude - 0.1;
                        double lng = latLngs.get(0).longitude - 0.1;
                        double lat1 = latLngs.get(latLngs.size()-1).latitude + 0.1;
                        double lng1 = latLngs.get(latLngs.size()-1).longitude + 0.1;


                        LatLngBounds latLngBounds1 = new LatLngBounds(new LatLng(lat,lng), new LatLng(lat1, lng1));

                        LatLngBounds latLngBounds = LatLngBounds.builder().include(initialLatLng).include(LastLatLng).build();
                        mgoogleMap.setLatLngBoundsForCameraTarget(latLngBounds);
                        mgoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds1, 50));


                        for (int i = 0; i < latLngs.size(); i++){
                            LatLng latLng = latLngs.get(i);

                            try {
                                getAddress(latLng.latitude, latLng.longitude);
                                rvLocationView.setAdapter(new ViewHistoryAdapter(getApplicationContext(), addr));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            Log.d("docdata", latLng.toString());
                        }



                    }
                    else{
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(),Toast.LENGTH_LONG).show();
                    }
                }

            }
        });
    }

    public void getAddress(double lat, double lng) throws IOException {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
        for (int i=0; i<addresses.size(); i++) {
            addr.add(addresses.get(i).getAddressLine(0));

            Log.d("IGA", "Address" + addr);


        }
    }

    private void getDistance(ArrayList<LatLng> latLngs) {
        float tempTotalDistance = 0f;

        for (int i =0; i < latLngs.size() -1; i++) {
            LatLng pointA = latLngs.get(i);
            LatLng pointB = latLngs.get(i+1);

            float[] results = new float[1];
            Location.distanceBetween (pointA.latitude, pointA.longitude, pointB.latitude, pointB.longitude, results);
            tempTotalDistance +=  results[0];
//            Log.d("Distance", "tempTotalDistance: "+String.valueOf(tempTotalDistance));
        }
        totalD = tempTotalDistance / 1000;
//                        Log.d("Distance", "totalD: "+String.valueOf(totalD));
        float dis = Float.parseFloat(String.format("%.1f", totalD));
        tvDis.setText(String.valueOf(dis));
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mgoogleMap = googleMap;
    }

}


