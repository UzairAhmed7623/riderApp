package com.example.dashboard1;

import android.app.ActivityOptions;
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
import android.util.Pair;
import android.view.View;
import android.view.animation.AnimationUtils;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.logging.Logger;

public class LoginActivity extends AppCompatActivity {

    private TextView tvForgotPass, tvSignUp, textView, tvInkHornSolution;
    private TextInputLayout etPassword;
    private TextView tvPhoneNumber;
    private CheckBox checkBox;
    private Button btnSignIn;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String password;
    private LottieAnimationView lottieLogin;
    private LinearLayout lottieLayoutLogin;
    private String checkBox1;
    static LoginActivity loginActivityInstance;

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

        tvPhoneNumber = (TextView) findViewById(R.id.tvPhoneNumber);
        textView = (TextView) findViewById(R.id.textView);
        tvInkHornSolution = (TextView) findViewById(R.id.tvInkHornSolution);
        etPassword =  (TextInputLayout) findViewById(R.id.etPassword);
        tvSignUp = (TextView)findViewById(R.id.tvSignUp);
        tvForgotPass = (TextView)findViewById(R.id.tvForgotPass);
        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        checkBox = (CheckBox) findViewById(R.id.checkbox);
        lottieLayoutLogin = (LinearLayout) findViewById(R.id.lottieLayoutLogin);
        lottieLogin = (LottieAnimationView) findViewById(R.id.lottieLogin);

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
                Intent loginIntent = new Intent(LoginActivity.this, SignUp.class);

                Pair[] pair = new Pair[4];
                pair[0] = new Pair<>(textView, "rider");
                pair[1] = new Pair<>(tvPhoneNumber, "phone");
                pair[2] = new Pair<>(btnSignIn, "signVerify");
                pair[3] = new Pair<>(tvInkHornSolution, "inkhorn");

                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this, pair);
                startActivity(loginIntent, activityOptions.toBundle());
            }
        });

        rememberLogin();

        if (firebaseAuth.getUid() != null){
            firebaseFirestore.collection("Users").document(firebaseAuth.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()){
                            String phone = documentSnapshot.getString("Phone");
                            String pass = documentSnapshot.getString("Pin");

                            tvPhoneNumber.setText(phone);

                            btnSignIn.setOnClickListener((View v) -> {
                                password = etPassword.getEditText().getText().toString();

                                if (etPassword.getEditText().getText().toString().isEmpty()) {
                                    Toast.makeText(LoginActivity.this, "Please write your pin!", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    lottieLayoutLogin.setVisibility(View.VISIBLE);
                                    lottieLogin.setVisibility(View.VISIBLE);

                                    if (password.equals(pass)) {
                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                lottieLayoutLogin.setVisibility(View.GONE);
                                                lottieLogin.setVisibility(View.GONE);
                                            }
                                        }, 2500);

                                        Handler handler1 = new Handler();
                                        handler1.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }, 2500);
                                    }
                                    else {
                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                lottieLayoutLogin.setVisibility(View.GONE);
                                                lottieLogin.setVisibility(View.GONE);
                                            }
                                        }, 2500);
                                        Toast.makeText(LoginActivity.this, "Wrong Pin!", Toast.LENGTH_SHORT).show();
                                    }
                                }

                            });
                        }
                        else {
                            Toast.makeText(LoginActivity.this, "Phone number not found!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
        else {
            Intent intent = new Intent(LoginActivity.this, SignUp.class);
            startActivity(intent);
        }


    }

    public void rememberLogin(){
        SharedPreferences sharedPreferences = getSharedPreferences("checkBox", MODE_PRIVATE);
        checkBox1 = sharedPreferences.getString("remember", "");
        if (checkBox1.equals("true")){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else if (checkBox1.equals("false")){
            Log.d("TAG", "Please SignIn.");
        }

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()){
                    SharedPreferences sharedPreferences = getSharedPreferences("checkBox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("remember", "true");
                    editor.apply();
//                    Toast.makeText(LoginActivity.this, "Checked", Toast.LENGTH_SHORT).show();
                }
                else if (!compoundButton.isChecked()){
                    SharedPreferences sharedPreferences = getSharedPreferences("checkBox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("remember", "false");
                    editor.apply();
//                    Toast.makeText(LoginActivity.this, "UnChecked", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        SplashScreen.getInstance().finish();
        super.onBackPressed();
    }
}