package com.example.dashboard1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.TaskExecutor;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class VerifyPhoneNumber extends AppCompatActivity {

    private TextView textView, tvInkHornSolutionVerify;
    private String verificationCodeBySystem;
    private PinView etOtp;
    private Button btnVerify;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private LottieAnimationView lottie_Verify_Phone;
    private LinearLayout lottieLayout_Verify_Phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone_number);

        getSupportActionBar().hide();

        firebaseAuth = firebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        textView = (TextView) findViewById(R.id.textView);
        tvInkHornSolutionVerify = (TextView) findViewById(R.id.tvInkHornSolutionVerify);
        etOtp = (PinView) findViewById(R.id.etOtp);
        btnVerify = (Button) findViewById(R.id.btnVerify);
        lottieLayout_Verify_Phone = (LinearLayout) findViewById(R.id.lottieLayout_Verify_Phone);
        lottie_Verify_Phone = (LottieAnimationView) findViewById(R.id.lottie_Verify_Phone);

        String phone = getIntent().getStringExtra("phone_number");

        verification(phone);

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otp = etOtp.getText().toString();
                if (otp.isEmpty() || otp.length() < 6){
                    etOtp.setError("Wrong OTP!");
                    etOtp.requestFocus();
                    return;
                }
                lottieLayout_Verify_Phone.setVisibility(View.VISIBLE);
                lottie_Verify_Phone.setVisibility(View.VISIBLE);

                verifyCode(otp);
            }
        });

    }

    private void verification(String phone) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phone)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(VerifyPhoneNumber.this) // Activity (for callback binding)
                        .setCallbacks(mCallbacks) // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationCodeBySystem = s;
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null){
                lottieLayout_Verify_Phone.setVisibility(View.VISIBLE);
                lottie_Verify_Phone.setVisibility(View.VISIBLE);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    lottieLayout_Verify_Phone.setVisibility(View.GONE);
                    lottie_Verify_Phone.setVisibility(View.GONE);
                }
            }, 2500);

            Handler handler1 = new Handler();
            handler1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(VerifyPhoneNumber.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(VerifyPhoneNumber.this, SignUp.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }, 2500);

        }
    };

    private void verifyCode(String code) {
        PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(verificationCodeBySystem, code);
        signInUserByCredientials(phoneAuthCredential);
    }

    private void signInUserByCredientials(PhoneAuthCredential phoneAuthCredential) {
        firebaseAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(VerifyPhoneNumber.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            lottieLayout_Verify_Phone.setVisibility(View.GONE);
                            lottie_Verify_Phone.setVisibility(View.GONE);
                        }
                    }, 2500);

                    String ph = task.getResult().getUser().getPhoneNumber();

                    firebaseFirestore.collection("Users").whereEqualTo("Phone", ph).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            if (task.isSuccessful()) {
                                if (task.getResult().size() > 0) {

                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            lottieLayout_Verify_Phone.setVisibility(View.GONE);
                                            lottie_Verify_Phone.setVisibility(View.GONE);
                                        }
                                    }, 2500);

                                    Intent intent = new Intent(VerifyPhoneNumber.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                }
                                else {
                                    Handler handler1 = new Handler();
                                    handler1.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent VerifyIntent = new Intent(VerifyPhoneNumber.this, Password_Creation.class);
                                            VerifyIntent.putExtra("phone", ph);
                                            startActivity(VerifyIntent);
                                            finish();
                                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                        }
                                    }, 2500);
                                }
                            }
                            else {
                                Snackbar.make(findViewById(android.R.id.content), task.getException().getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(getResources().getColor(R.color.myColor)).show();
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(VerifyPhoneNumber.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}