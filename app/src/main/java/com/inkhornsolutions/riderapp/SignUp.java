package com.inkhornsolutions.riderapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class SignUp extends AppCompatActivity {

    private TextView textView, tvInkHornSolutionSign;
    private TextInputLayout etPhoneNumber;
    private Button btnVerify;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

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

        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String phone_number = "+92" + etPhoneNumber.getEditText().getText().toString();

                if (etPhoneNumber.getEditText().getText().toString().isEmpty() || etPhoneNumber.getEditText().getText().toString().length() < 10) {
                    Snackbar.make(findViewById(android.R.id.content), "Please write a valid phone number!", Snackbar.LENGTH_LONG).setBackgroundTint(getResources().getColor(R.color.myColor)).show();
                }

                else {

                    progressDialog.show();
                    progressDialog.setCancelable(false);
                    progressDialog.setContentView(R.layout.progress_layout);
                    progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                    if (firebaseAuth.getUid() != null) {

                    firebaseFirestore.collection("Users").whereEqualTo("Phone", phone_number).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            if (task.isSuccessful()) {
                                if (task.getResult().size() > 0) {

                                    progressDialog.dismiss();


                                    Snackbar.make(findViewById(android.R.id.content), "Phone number already registered!", Snackbar.LENGTH_LONG).setBackgroundTint(getResources().getColor(R.color.myColor)).show();
                                }
                            } else {
                                progressDialog.dismiss();

                                Snackbar.make(findViewById(android.R.id.content), task.getException().getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(getResources().getColor(R.color.myColor)).show();
                            }
                        }
                    });
                }
                    else {

                        progressDialog.dismiss();

                        Handler handler1 = new Handler();
                        handler1.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent SignUpIntent = new Intent(SignUp.this, VerifyPhoneNumber.class);
                                SignUpIntent.putExtra("phone_number", phone_number);
                                startActivity(SignUpIntent);
                                finish();
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            }
                        }, 1000);
                    }

                }

            }
        });
    }
}
