package com.example.dashboard1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignUp extends AppCompatActivity {

    private TextInputLayout etPhoneNumber, etPassword;
    private Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etPhoneNumber = (TextInputLayout) findViewById(R.id.etPhoneNumber);
        etPassword =  (TextInputLayout) findViewById(R.id.etPassword);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String phone_number = etPhoneNumber.getEditText().getText().toString();
                String password = etPassword.getEditText().getText().toString();

                if (etPhoneNumber.getEditText().getText().toString().isEmpty()) {
                    etPhoneNumber.setError("Please write your email!");
                }
                else if (etPassword.getEditText().getText().toString().isEmpty()) {
                    etPassword.setError("Please write your password!");
                }
                else {
                    Intent intent = new Intent(SignUp.this, VerifyPhoneNumber.class);
                    intent.putExtra("phone_number", phone_number);
                    intent.putExtra("password", password);
                    startActivity(intent);
                }
            }
        });
    }
}