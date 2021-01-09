package com.example.dashboard1;

import androidx.annotation.NonNull;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextView tvForgotPass, tvSignUp;
    private EditText etEmail, etPassword;
    private Button login;
    private CheckBox checkBox;
    private FirebaseAuth firebaseAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvSignUp = (TextView)findViewById(R.id.tvSignUp);
        tvForgotPass = (TextView)findViewById(R.id.tvForgotPass);
        login = findViewById(R.id.login);
        checkBox = findViewById(R.id.checkbox);

        String forgotPass = "Forgot password?";
        tvForgotPass.setText(boldSignUptext(forgotPass));

        String signup = "Don't have an account? SignUp";
        tvSignUp.setText(boldSignUptext(signup));

        tvForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ForgotPassword.class);
                startActivity(intent);
            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUp.class);
                startActivity(intent);
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();

        rememberLogin();

        login.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String passWord = etPassword.getText().toString();

            if (etEmail.getText().toString().isEmpty()) {
                etEmail.setError("Please write your email!");
            }
            else if (etPassword.getText().toString().isEmpty()) {
                etPassword.setError("Please write your password!");

            }
            else {
                ProgressDialog dialog = ProgressDialog.show(LoginActivity.this, "Loading", "Please wait...", true);
                firebaseAuth.signInWithEmailAndPassword(email, passWord).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            String Id = firebaseUser.getUid();
                            Toast.makeText(LoginActivity.this, Id, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
            }
        });

    }

    public void rememberLogin(){
        SharedPreferences sharedPreferences = getSharedPreferences("checkBox", MODE_PRIVATE);
        String checkBox1 = sharedPreferences.getString("remember", "");
        if (checkBox1.equals("true")){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else if (checkBox1.equals("false")){
            Toast.makeText(this, "Please Sign In.", Toast.LENGTH_SHORT).show();
        }

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()){
                    SharedPreferences sharedPreferences = getSharedPreferences("checkBox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("remember", "true");
                    editor.apply();
                    Toast.makeText(LoginActivity.this, "Checked", Toast.LENGTH_SHORT).show();
                }
                else if (!compoundButton.isChecked()){
                    SharedPreferences sharedPreferences = getSharedPreferences("checkBox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("remember", "false");
                    editor.apply();
                    Toast.makeText(LoginActivity.this, "UnChecked", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public SpannableString boldSignUptext(String text){

        SpannableString spannable = new SpannableString(text);

        if (text.length() <= 16){
            StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
            spannable.setSpan(styleSpan, 0, 16, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            UnderlineSpan underlineSpan = new UnderlineSpan();
            spannable.setSpan(underlineSpan, 0, 16, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        else
        {
            StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
            spannable.setSpan(styleSpan, 23, 29, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            UnderlineSpan underlineSpan = new UnderlineSpan();
            spannable.setSpan(underlineSpan, 23, 29, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }

}