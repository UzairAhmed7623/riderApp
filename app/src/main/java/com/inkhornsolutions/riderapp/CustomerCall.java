package com.inkhornsolutions.riderapp;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.TextView;

public class CustomerCall extends AppCompatActivity {

    private TextView txtTime, txtDistance, txtAddress;
    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_call);


        txtTime = (TextView) findViewById(R.id.txtTime);
        txtDistance = (TextView) findViewById(R.id.txtDistance);
        txtAddress = (TextView) findViewById(R.id.txtAddress);

        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        if (getIntent()!=null){
            double lat = getIntent().getDoubleExtra("lat", -1.0);
            double lng = getIntent().getDoubleExtra("lng", -1.0);

//            getDirection(lat, lng);
        }

    }

//    private void getDirection(double lat, double lng) {
//
//        Toast.makeText(this, ""+ Common.latLng1, Toast.LENGTH_SHORT).show();
//
//        String requestApi = null;
//        try {
//            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
//                    "mode=driving&" +
//                    "transit_routing_preference=less_driving&" +
//                    "origin=" + Common.latLng1.latitude + "," + Common.latLng1.longitude + "&" +
//                    "destination=" + lat + "," + lng + "&" +
//                    "key=" + "AIzaSyDl7YXtTZQNBkthV3PjFS0fQOKvL8SIR7k";
//
//            Log.d("TAG", requestApi);
//
//            mService.getPath(requestApi).enqueue(new Callback<String>() {
//
//                @Override
//
//                public void onResponse(Call<String> call, Response<String> response) {
//
//                    try {
//                        JSONObject jsonObject = new JSONObject(response.body().toString());
//                        JSONArray routes = jsonObject.getJSONArray("routes");
//
//                        JSONObject object = routes.getJSONObject(0);
//                        JSONArray legs = object.getJSONArray("legs");
//                        JSONObject legsObject = legs.getJSONObject(0);
//
//                        JSONObject distance = legsObject.getJSONObject("distance");
//                        txtDistance.setText(distance.getString("text"));
//
//                        JSONObject time = legsObject.getJSONObject("duration");
//                        txtTime.setText(time.getString("text"));
//
//                        String address = legsObject.getString("end_address");
//                        txtAddress.setText(address);
//                    }
//                    catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<String> call, Throwable t) {
//                    Toast.makeText(CustomerCall.this, t.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    protected void onStop() {
        mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        mediaPlayer.release();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer.start();

    }
}