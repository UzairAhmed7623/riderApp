package com.inkhornsolutions.riderapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class New_Pin extends AppCompatActivity {

    private TextView textView, tvInkHornSolutionPass;
    private TextInputLayout etPin, etRe_Pin;
    private Button btnCreate_Pin;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String phone;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new__pin);

        getSupportActionBar().hide();

        textView = (TextView) findViewById(R.id.textView);
        tvInkHornSolutionPass = (TextView) findViewById(R.id.tvInkHornSolutionPass);
        etPin = (TextInputLayout) findViewById(R.id.etPin);
        etRe_Pin = (TextInputLayout) findViewById(R.id.etRe_Pin);
        btnCreate_Pin = (Button) findViewById(R.id.btnCreate_Pin);

        progressDialog = new ProgressDialog(this);

        phone = getIntent().getStringExtra("phone");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        btnCreate_Pin.setOnClickListener(new View.OnClickListener() {
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
                    Toast.makeText(New_Pin.this, "Please enter same pin both times!", Toast.LENGTH_LONG).show();
                }
                else {

                    if (!isConnected()){

                        progressDialog.show();
                        progressDialog.setCancelable(false);
                        progressDialog.setContentView(R.layout.progress_layout);
                        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                        progressDialog.dismiss();

                        Snackbar.make(findViewById(android.R.id.content), "Internet not connected!", Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).show();

                    }
                    if (firebaseAuth.getUid() != null){

                        progressDialog.show();
                        progressDialog.setCancelable(false);
                        progressDialog.setContentView(R.layout.progress_layout);
                        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                        HashMap<String, Object> new_User = new HashMap<>();
                        new_User.put("Pin", pin);

                        firebaseFirestore.collection("Users").document(firebaseAuth.getUid()).update(new_User).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    progressDialog.dismiss();

                                    progressDialog.show();
                                    progressDialog.setCancelable(false);
                                    progressDialog.setContentView(R.layout.good_progress_layout);
                                    progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {

                                            progressDialog.dismiss();

                                        }
                                    }, 2500);
                                    Handler handler1 = new Handler();
                                    handler1.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(New_Pin.this, LoginActivity.class);
                                            startActivity(intent);
                                            finish();
                                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                        }
                                    }, 2500);
                                } else {

                                    progressDialog.dismiss();

                                    Toast.makeText(New_Pin.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                    else {
                        progressDialog.show();
                        progressDialog.setCancelable(false);
                        progressDialog.setContentView(R.layout.progress_layout);
                        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                progressDialog.dismiss();

                                Snackbar.make(findViewById(android.R.id.content), "Account not found!", Snackbar.LENGTH_LONG).setBackgroundTint(getColor(R.color.myColor)).show();
                            }
                        },1000);
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