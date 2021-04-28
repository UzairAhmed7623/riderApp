package com.inkhornsolutions.riderapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ForgotPassword extends AppCompatActivity {

    private TextView textView, textView1, tvInkHornSolutionForgot;
    private EditText etPhoneNumberForgot;
    private Button btnVerifyForgot;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        textView = (TextView) findViewById(R.id.textView);
        textView1 = (TextView) findViewById(R.id.textView1);
        tvInkHornSolutionForgot = (TextView) findViewById(R.id.tvInkHornSolutionForgot);
        btnVerifyForgot = (Button) findViewById(R.id.btnVerifyForgot);
        etPhoneNumberForgot = (EditText) findViewById(R.id.etPhoneNumberForgot);

        progressDialog = new ProgressDialog(this);

        firebaseAuth = firebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        String phone = getIntent().getStringExtra("phone");
        etPhoneNumberForgot.setText(phone);

        btnVerifyForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (etPhoneNumberForgot.getText().toString().isEmpty() || etPhoneNumberForgot.getText().toString().length() < 10) {
                    Snackbar.make(findViewById(android.R.id.content), "Please write a valid phone number!", Snackbar.LENGTH_LONG).setBackgroundTint(getResources().getColor(R.color.myColor)).show();
                }
                else {

                    progressDialog.show();
                    progressDialog.setCancelable(false);
                    progressDialog.setContentView(R.layout.progress_layout);
                    progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                    if (firebaseAuth.getUid() != null) {

                        firebaseFirestore.collection("Users").whereEqualTo("phoneNumber", phone).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                if (task.isSuccessful()) {
                                    if (task.getResult().size() > 0) {

                                        progressDialog.dismiss();

                                        Handler handler1 = new Handler();
                                        handler1.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent SignUpIntent = new Intent(ForgotPassword.this, Verify_Phone_For_Pin.class);
                                                SignUpIntent.putExtra("phone", phone);
                                                startActivity(SignUpIntent);
                                                finish();
                                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                            }
                                        }, 1000);
                                    }
                                }
                                else {

                                    progressDialog.dismiss();

                                    Snackbar.make(findViewById(android.R.id.content), task.getException().getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(getResources().getColor(R.color.myColor)).show();
                                }
                            }
                        });
                    }
                    else {

                        progressDialog.dismiss();

                        Snackbar.make(findViewById(android.R.id.content), "Phone number not found!", Snackbar.LENGTH_LONG).setBackgroundTint(getResources().getColor(R.color.myColor)).show();

                    }

                }
            }
        });

    }
}