package com.example.dashboard1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.TaskExecutor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
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

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class VerifyPhoneNumber extends AppCompatActivity {

    private String verificationCodeBySystem;
    private TextInputLayout etOtp;
    private Button btnVerify;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressbar;
    private FirebaseFirestore firebaseFirestore;
    private String pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone_number);

        getSupportActionBar().hide();

        firebaseAuth = firebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        etOtp = (TextInputLayout) findViewById(R.id.etOtp);
        btnVerify = (Button) findViewById(R.id.btnVerify);
        progressbar = (ProgressBar) findViewById(R.id.progressbar);

        String phone = getIntent().getStringExtra("phone_number");

        verification(phone);

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otp = etOtp.getEditText().getText().toString();
                if (otp.isEmpty() || otp.length() < 6){
                    etOtp.setError("Wrong OTP!");
                    etOtp.requestFocus();
                    return;
                }
                progressbar.setVisibility(View.VISIBLE);
                verifyCode(otp);
            }
        });

    }

    private void verification(String phone) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phone)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(VerifyPhoneNumber.this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks) // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationCodeBySystem = s;
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null){
                progressbar.setVisibility(View.VISIBLE);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(VerifyPhoneNumber.this, e.getMessage(), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(VerifyPhoneNumber.this, SignUp.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
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

                    String ph = task.getResult().getUser().getPhoneNumber();

                    Intent intent = new Intent(VerifyPhoneNumber.this, Password_Creation.class);
                    intent.putExtra("phone", ph);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(VerifyPhoneNumber.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}