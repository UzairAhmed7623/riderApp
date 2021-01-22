package com.example.dashboard1;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmailAddress, etPhoneNumber;
    private Button btnSave;
    private ImageButton ivAddImage;
    private CircleImageView ivProfile;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private DocumentReference documentReference;
    private LottieAnimationView lottieProfile;
    private LinearLayout lottieLayout_Profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#02AA4E")));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("ProfileDetail");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());

        lottieLayout_Profile = (LinearLayout) findViewById(R.id.lottieLayout_Profile);
        lottieProfile = (LottieAnimationView) findViewById(R.id.lottieProfile);
        etFirstName = (EditText) findViewById(R.id.etFirstName);
        etLastName = (EditText) findViewById(R.id.etLastName);
        etEmailAddress = (EditText) findViewById(R.id.etEmailAddress);
        etPhoneNumber = (EditText) findViewById(R.id.etPhoneNumber);
        btnSave = (Button) findViewById(R.id.btnSave);
        ivAddImage = (ImageButton) findViewById(R.id.ivAddImage);
        ivProfile = (CircleImageView) findViewById(R.id.ivProfile);

        lottieProfile.setVisibility(View.VISIBLE);
        lottieLayout_Profile.setVisibility(View.VISIBLE);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()){
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                lottieProfile.setVisibility(View.GONE);
                                lottieLayout_Profile.setVisibility(View.GONE);
                                String phone = documentSnapshot.getString("Phone");
                                etPhoneNumber.setText(phone);
                            }
                        }, 2500);
                    }
                    else {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                lottieProfile.setVisibility(View.GONE);
                                lottieLayout_Profile.setVisibility(View.GONE);
                            }
                        }, 2500);
                        Toast.makeText(Profile.this, "No data found!", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            lottieProfile.setVisibility(View.GONE);
                            lottieLayout_Profile.setVisibility(View.GONE);
                        }
                    }, 2500);
                    Log.d("TAG", task.getException().getMessage());
                }
            }
        });

        ivAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Profile.this, "Clicked!", Toast.LENGTH_SHORT).show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fName = etFirstName.getText().toString().trim();
                String lName = etLastName.getText().toString().trim();
                String email = etEmailAddress.getText().toString().trim();

                if (etFirstName.getText().toString().isEmpty()){
                    etFirstName.setError("Please your first name!");
                }
                else if (etLastName.getText().toString().isEmpty()){
                    etLastName.setError("Please your last name!");
                }
                else if (etEmailAddress.getText().toString().isEmpty()){
                    etEmailAddress.setError("Please your email!");
                }
                else {

                    lottieProfile.setVisibility(View.VISIBLE);
                    lottieLayout_Profile.setVisibility(View.VISIBLE);

                    HashMap<String, Object> userProfile = new HashMap<>();
                    userProfile.put("First Name", fName);
                    userProfile.put("Last Name", lName);
                    userProfile.put("Email Address", email);

                    documentReference.update(userProfile).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        lottieProfile.setVisibility(View.GONE);
                                        lottieLayout_Profile.setVisibility(View.GONE);
                                    }
                                }, 2500);
                                Toast.makeText(Profile.this, "Profile created successfully!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Profile.this, MainActivity.class);
                                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(Profile.this);
                                startActivity(intent, activityOptions.toBundle());
                            }
                            else {
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        lottieProfile.setVisibility(View.GONE);
                                        lottieLayout_Profile.setVisibility(View.GONE);
                                    }
                                }, 2500);
                                Toast.makeText(Profile.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });


    }
}