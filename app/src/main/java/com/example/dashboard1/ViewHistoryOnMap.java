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
    private final List<Marker> markerList = new ArrayList<>();
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private DocumentReference documentReference;
    private RecyclerView rvLocationView;
    private TextView tvDis;
    private float totalD = 0f;
    private Double dolkm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_history_on_map);

        tvDis = (TextView)findViewById(R.id.tvDis);

        rvLocationView = (RecyclerView) findViewById(R.id.rvLocationView);
        rvLocationView.setLayoutManager(new LinearLayoutManager(this));

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

                        for (int i = 0; i < latLngs.size(); i++){
                            LatLng latLng = latLngs.get(i);
                            mgoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                            markerList.add(marker);
                            PolylineOptions polylineOptions = new PolylineOptions().addAll(latLngs).clickable(true);
                            polyline = mgoogleMap.addPolyline(polylineOptions);
                            polyline.setColor(Color.BLUE);
                            polyline.setWidth(10);

                            if (marker != null)
                            {
                                marker.remove();
                            }
                            marker = mgoogleMap.addMarker(new MarkerOptions().position(new LatLng(latLng.latitude,latLng.longitude)).title("Marker in current position"));

                            try {
                                getAddress(latLng.latitude, latLng.longitude);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            getDistance(latLngs);

                            mgoogleMap.moveCamera(CameraUpdateFactory.zoomTo(12));
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
            List<String> addr = new ArrayList<>();
            addr.add(addresses.get(i).getAddressLine(0));

            Log.d("IGA", "Address" + addr);


            rvLocationView.setAdapter(new ViewHistoryAdapter(getApplicationContext(), addr));
        }
    }

    private void getDistance(ArrayList<LatLng> latLngs) {
        float tempTotalDistance = 0f;

        for (int i =0; i < latLngs.size() -1; i++) {
            LatLng pointA = latLngs.get(i);
            LatLng pointB = latLngs.get(i+1);

//          Log.d("ArrayPoints", "Point A" + String.valueOf(pointA.latitude+" "+pointA.longitude));
//          Log.d("ArrayPoints", "PointB" + String.valueOf(pointB.latitude+" "+pointB.longitude));

//          LatLng pointA = new LatLng(points.get(i).get("latitude"), points.get(i).get("longitude"));
//          LatLng pointB = new LatLng(points.get(i+1).get("latitude"), points.get(i+1).get("longitude"));
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

    public void test(){
        firebaseFirestore.collection("user").document(firebaseAuth.getUid()).collection("location").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    List<String> docList = new ArrayList<>();
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        Log.d("TAG",queryDocumentSnapshot.getId());
                        docList.add(queryDocumentSnapshot.getId());
                        rvLocationView.setAdapter(new DocumentViewAdapter(getApplicationContext(),docList));
                    }
                }
                else {
                    Log.d("TAG",task.getException().toString());
                }
            }
        });
    }
}


