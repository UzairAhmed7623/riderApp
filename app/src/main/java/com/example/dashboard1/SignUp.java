package com.example.dashboard1;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class SignUp extends AppCompatActivity {

    private TextView textView, tvInkHornSolutionSign;
    private TextInputLayout etPhoneNumber;
    private Button btnVerify;
    private FirebaseFirestore firebaseFirestore;
    private DocumentReference documentReference;
    private FirebaseAuth firebaseAuth;
    private LottieAnimationView lottieSignUp;
    private LinearLayout lottieLayoutSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
//        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#02AA4E")));
//        actionBar.setTitle("Verification");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textView = (TextView) findViewById(R.id.textView);
        tvInkHornSolutionSign = (TextView) findViewById(R.id.tvInkHornSolutionSign);
        etPhoneNumber = (TextInputLayout) findViewById(R.id.etPhoneNumber);
        btnVerify = (Button) findViewById(R.id.btnVerify);
        lottieLayoutSignUp = (LinearLayout) findViewById(R.id.lottieLayoutSignUp);
        lottieSignUp = (LottieAnimationView) findViewById(R.id.lottieSignUp);

        firebaseAuth = firebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        documentReference = firebaseFirestore.collection("Users").document();

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String phone_number = "+92" + etPhoneNumber.getEditText().getText().toString();

                if (etPhoneNumber.getEditText().getText().toString().isEmpty() || etPhoneNumber.getEditText().getText().toString().length() < 10) {
                    etPhoneNumber.setError("Please write a valid phone!");
                }

                else {

                    lottieLayoutSignUp.setVisibility(View.VISIBLE);
                    lottieSignUp.setVisibility(View.VISIBLE);

                    firebaseFirestore.collection("Users").whereEqualTo("Phone", phone_number).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            if (task.isSuccessful()) {
                                if (task.getResult().size() > 0) {

                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            lottieLayoutSignUp.setVisibility(View.GONE);
                                            lottieSignUp.setVisibility(View.GONE);
                                        }
                                    }, 2500);

                                    Handler handler1 = new Handler();
                                    handler1.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            etPhoneNumber.setError("Phone number already registered!");
                                        }
                                    }, 2500);
                                }
                                else {
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            lottieLayoutSignUp.setVisibility(View.GONE);
                                            lottieSignUp.setVisibility(View.GONE);
                                        }
                                    }, 2500);

                                    Handler handler1 = new Handler();
                                    handler1.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent SignUpIntent = new Intent(SignUp.this, VerifyPhoneNumber.class);
                                            SignUpIntent.putExtra("phone_number", phone_number);

                                            Pair[] pair = new Pair[4];
                                            pair[0] = new Pair<>(textView, "rider");
                                            pair[1] = new Pair<>(etPhoneNumber, "phone");
                                            pair[2] = new Pair<>(btnVerify, "signVerify");
                                            pair[3] = new Pair<>(tvInkHornSolutionSign, "inkhorn");

                                            ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(SignUp.this, pair);
                                            startActivity(SignUpIntent, activityOptions.toBundle());
                                        }
                                    }, 2500);
                                }
                            }
                            else {
                                Toast.makeText(SignUp.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

            }
        });
    }
}
