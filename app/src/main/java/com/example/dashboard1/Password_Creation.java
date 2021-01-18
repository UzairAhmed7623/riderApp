package com.example.dashboard1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class Password_Creation extends AppCompatActivity {

    private TextInputLayout etPin, etRe_Pin;
    private Button btnSignUp;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressbar;
    private FirebaseFirestore firebaseFirestore;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password__creation);

        getSupportActionBar().hide();

        etPin =  (TextInputLayout) findViewById(R.id.etPin);
        etRe_Pin = (TextInputLayout) findViewById(R.id.etRe_Pin);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);

        phone = getIntent().getStringExtra("phone");

        firebaseAuth = firebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String pin = etPin.getEditText().getText().toString();
                String etRePin = etRe_Pin.getEditText().getText().toString();

                if (etPin.getEditText().getText().toString().isEmpty()) {
                    etPin.setError("Please write your email!");
                }
                else if (etRe_Pin.getEditText().getText().toString().isEmpty()) {
                    etRe_Pin.setError("Please write your password!");
                }
                else if (!pin.equals(etRePin)){
                    Toast.makeText(Password_Creation.this, "Please enter same pin both times!", Toast.LENGTH_LONG).show();
                }
                else {
                    HashMap<String, Object> new_User = new HashMap<>();
                    new_User.put("Phone", phone);
                    new_User.put("Pin", pin);

                    firebaseFirestore.collection("Users").document(firebaseAuth.getUid()).set(new_User).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Intent intent = new Intent(Password_Creation.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                            else {
                                Toast.makeText(Password_Creation.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }
            }
        });

    }
}