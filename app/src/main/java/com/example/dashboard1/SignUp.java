package com.example.dashboard1;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

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

    private TextInputLayout etPhoneNumber;
    private Button btnVerify;
    private FirebaseFirestore firebaseFirestore;
    private DocumentReference documentReference;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
//        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#02AA4E")));
//        actionBar.setTitle("Verification");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etPhoneNumber = (TextInputLayout) findViewById(R.id.etPhoneNumber);
        btnVerify = (Button) findViewById(R.id.btnVerify);

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

                    firebaseFirestore.collection("Users").whereEqualTo("Phone", phone_number).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            if (task.isSuccessful()) {
                                if (task.getResult().size() > 0)
                                {
                                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                        etPhoneNumber.setError("Phone number already registered!");
                                    }
                                }
                                else {
                                       Intent intent = new Intent(SignUp.this, VerifyPhoneNumber.class);
                                       intent.putExtra("phone_number", phone_number);
                                       startActivity(intent);
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