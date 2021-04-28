package com.inkhornsolutions.riderapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private TextView tvForgotPass, textView, tvInkHornSolution;
    private EditText etPhoneNumber;
    private TextInputLayout etPassword;
    private CheckBox checkBox;
    private Button btnSignIn;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String Pin;
    private String checkBox1, phone;
    static LoginActivity loginActivityInstance;
    private ProgressDialog progressDialog;

    public static LoginActivity getInstance() {
        return loginActivityInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        loginActivityInstance = this;

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        etPhoneNumber = (EditText) findViewById(R.id.etPhoneNumber);
        textView = (TextView) findViewById(R.id.textView);
        tvInkHornSolution = (TextView) findViewById(R.id.tvInkHornSolution);
        etPassword = (TextInputLayout) findViewById(R.id.etPassword);
        tvForgotPass = (TextView) findViewById(R.id.tvForgotPass);
        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        checkBox = (CheckBox) findViewById(R.id.checkbox);

        String forgotPass = "Forgot Pin?";
        tvForgotPass.setText(boldSignUptext(forgotPass));

        progressDialog = new ProgressDialog(this);


        tvForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ForgotPassword.class);
                intent.putExtra("phone", phone);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        rememberLogin();

        if (firebaseAuth.getUid() != null) {
            firebaseFirestore.collection("Users").document(firebaseAuth.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            phone = documentSnapshot.getString("phoneNumber");
                            String pin = documentSnapshot.getString("Pin");

                            etPhoneNumber.setText(phone);

                            btnSignIn.setOnClickListener((View v) -> {
                                Pin = etPassword.getEditText().getText().toString();

                                if (etPassword.getEditText().getText().toString().isEmpty()) {
                                    Toast.makeText(LoginActivity.this, "Please write your pin!", Toast.LENGTH_SHORT).show();
                                } else {

                                    progressDialog.show();
                                    progressDialog.setCancelable(false);
                                    progressDialog.setContentView(R.layout.progress_layout);
                                    progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                                    if (Pin.equals(pin)) {

                                        progressDialog.dismiss();

                                        Handler handler1 = new Handler();
                                        handler1.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                            }
                                        }, 2500);
                                    }
                                    else {

                                        progressDialog.dismiss();

                                        Toast.makeText(LoginActivity.this, "Wrong Pin!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else {

                            progressDialog.dismiss();

                            Toast.makeText(LoginActivity.this, "Phone number not found!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        } else {
            Intent intent = new Intent(LoginActivity.this, SignUp.class);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    public void rememberLogin() {
        SharedPreferences sharedPreferences = getSharedPreferences("checkBox", MODE_PRIVATE);
        checkBox1 = sharedPreferences.getString("remember", "");
        if (checkBox1.equals("true")) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else if (checkBox1.equals("false")) {
            Log.d("TAG", "Please SignIn.");
        }

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    SharedPreferences sharedPreferences = getSharedPreferences("checkBox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("remember", "true");
                    editor.apply();
//                    Toast.makeText(LoginActivity.this, "Checked", Toast.LENGTH_SHORT).show();
                } else if (!compoundButton.isChecked()) {
                    SharedPreferences sharedPreferences = getSharedPreferences("checkBox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("remember", "false");
                    editor.apply();
//                    Toast.makeText(LoginActivity.this, "UnChecked", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public SpannableString boldSignUptext(String text) {

        SpannableString spannable = new SpannableString(text);

        if (text.length() <= 11) {
            StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
            spannable.setSpan(styleSpan, 0, 11, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            UnderlineSpan underlineSpan = new UnderlineSpan();
            spannable.setSpan(underlineSpan, 0, 11, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        } else {
            StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
            spannable.setSpan(styleSpan, 23, 29, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            UnderlineSpan underlineSpan = new UnderlineSpan();
            spannable.setSpan(underlineSpan, 23, 29, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }
}