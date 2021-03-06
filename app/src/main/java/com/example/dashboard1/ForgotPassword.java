package com.example.dashboard1;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ForgotPassword extends AppCompatActivity {

    private TextView textView, textView1, tvInkHornSolutionForgot;
    private EditText etPhoneNumberForgot;
    private Button btnVerifyForgot;
    private LinearLayout lottieLayoutForgot;
    private LottieAnimationView lottieForgot;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

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
        lottieLayoutForgot = (LinearLayout) findViewById(R.id.lottieLayoutForgot);
        lottieForgot = (LottieAnimationView) findViewById(R.id.lottieForgot);

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

                    lottieLayoutForgot.setVisibility(View.VISIBLE);
                    lottieForgot.setVisibility(View.VISIBLE);
                    etPhoneNumberForgot.setEnabled(false); btnVerifyForgot.setEnabled(false);

                    if (firebaseAuth.getUid() != null) {

                        firebaseFirestore.collection("Users").whereEqualTo("Phone", phone).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                if (task.isSuccessful()) {
                                    if (task.getResult().size() > 0) {

                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                lottieLayoutForgot.setVisibility(View.GONE);
                                                lottieForgot.setVisibility(View.GONE);
                                                etPhoneNumberForgot.setEnabled(true); btnVerifyForgot.setEnabled(true);

                                            }
                                        }, 2500);

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
                                        }, 2500);
                                    }
                                } else {
                                    Snackbar.make(findViewById(android.R.id.content), task.getException().getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(getResources().getColor(R.color.myColor)).show();
                                }
                            }
                        });
                    }
                    else {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                lottieLayoutForgot.setVisibility(View.GONE);
                                lottieForgot.setVisibility(View.GONE);
                                etPhoneNumberForgot.setEnabled(true); btnVerifyForgot.setEnabled(true);
                            }
                        }, 2500);

                        Snackbar.make(findViewById(android.R.id.content), "Phone number not found!", Snackbar.LENGTH_LONG).setBackgroundTint(getResources().getColor(R.color.myColor)).show();

                    }

                }
            }
        });

    }
}