package com.example.dashboard1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.net.InetAddress;
import java.util.HashMap;

public class Password_Creation extends AppCompatActivity {

    private TextInputLayout etPin, etRe_Pin;
    private Button btnSignUp;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressbar;
    private FirebaseFirestore firebaseFirestore;
    private String phone;
    private LottieAnimationView lottiePasswordCreation, lottiePasswordCreation2;
    private LinearLayout lottieLayoutPassword, lottieLayoutPassword2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password__creation);

        getSupportActionBar().hide();

        etPin =  (TextInputLayout) findViewById(R.id.etPin);
        etRe_Pin = (TextInputLayout) findViewById(R.id.etRe_Pin);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        lottieLayoutPassword = (LinearLayout) findViewById(R.id.lottieLayoutPassword);
        lottiePasswordCreation = (LottieAnimationView) findViewById(R.id.lottiePasswordCreation);
        lottieLayoutPassword2 = (LinearLayout) findViewById(R.id.lottieLayoutPassword2);
        lottiePasswordCreation2 = (LottieAnimationView) findViewById(R.id.lottiePasswordCreation2);

        phone = getIntent().getStringExtra("phone");

        firebaseAuth = firebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String pin = etPin.getEditText().getText().toString();
                String etRePin = etRe_Pin.getEditText().getText().toString();

                if (etPin.getEditText().getText().toString().isEmpty()) {
                    etPin.setError("Please write your pin!");
                }
                else if (etRe_Pin.getEditText().getText().toString().isEmpty()) {
                    etRe_Pin.setError("Please write your pin!");
                }
                else if (!pin.equals(etRePin)){
                    Toast.makeText(Password_Creation.this, "Please enter same pin both times!", Toast.LENGTH_LONG).show();
                }
                else {

                    if (!isConnected()){
                        lottiePasswordCreation2.setVisibility(View.VISIBLE);
                        lottieLayoutPassword2.setVisibility(View.VISIBLE);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    lottieLayoutPassword2.setVisibility(View.GONE);
                                    lottiePasswordCreation2.setVisibility(View.GONE);
                                    Snackbar.make(findViewById(android.R.id.content), "Internet not connected!", Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).show();
                                }
                            },2500);
                    }
                    if (firebaseAuth.getUid() != null){
                        lottiePasswordCreation2.setVisibility(View.VISIBLE);
                        lottieLayoutPassword2.setVisibility(View.VISIBLE);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                lottieLayoutPassword2.setVisibility(View.GONE);
                                lottiePasswordCreation2.setVisibility(View.GONE);
                            }
                        },2500);

                        Handler handler1 = new Handler();
                        handler1.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                HashMap<String, Object> new_User = new HashMap<>();
                                new_User.put("Phone", phone);
                                new_User.put("Pin", pin);

                                firebaseFirestore.collection("Users").document(firebaseAuth.getUid()).set(new_User).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){

                                            lottiePasswordCreation.setVisibility(View.VISIBLE);
                                            lottieLayoutPassword.setVisibility(View.VISIBLE);
                                            Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    lottieLayoutPassword.setVisibility(View.GONE);
                                                    lottiePasswordCreation.setVisibility(View.GONE);
                                                }
                                            },2500);
                                            Handler handler1 = new Handler();
                                            handler1.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Intent intent = new Intent(Password_Creation.this, LoginActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                }
                                            },2500);
                                        }
                                        else {

                                            Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    lottieLayoutPassword2.setVisibility(View.GONE);
                                                    lottiePasswordCreation2.setVisibility(View.GONE);
                                                }
                                            },2500);
                                            Toast.makeText(Password_Creation.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        }, 2500);

                    }
                    else {
                        lottiePasswordCreation2.setVisibility(View.VISIBLE);
                        lottieLayoutPassword2.setVisibility(View.VISIBLE);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                lottieLayoutPassword2.setVisibility(View.GONE);
                                lottiePasswordCreation2.setVisibility(View.GONE);
                                Snackbar.make(findViewById(android.R.id.content), "Account not found!", Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).show();
                            }
                        },2500);
                    }
                }
            }
        });

    }

    public boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }
}