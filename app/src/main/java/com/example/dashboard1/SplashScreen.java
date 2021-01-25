package com.example.dashboard1;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends AppCompatActivity {

    private LottieAnimationView lottieSondi, lottiesplash;
    private FirebaseAuth firebaseAuth;
    static SplashScreen splashScreenInstance;

    public static SplashScreen getInstance() {
        return splashScreenInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        splashScreenInstance = this;

        getSupportActionBar().hide();

        firebaseAuth = FirebaseAuth.getInstance();

        lottiesplash =(LottieAnimationView) findViewById(R.id.lottiesplash);
        lottieSondi = (LottieAnimationView) findViewById(R.id.lottieSondi);

        lottiesplash.setVisibility(View.VISIBLE);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                lottieSondi.setVisibility(View.VISIBLE);
            }
        },5000);

        if (firebaseAuth.getUid() != null){
            Handler handler2 = new Handler();
            handler2.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    lottieSplash.setVisibility(View.GONE);

                    Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }, 7000);

        }
        else {
            Handler handler2 = new Handler();
            handler2.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    lottieSplash.cancelAnimation();
                    Intent intent = new Intent(SplashScreen.this, SignUp.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }, 7000);
        }
    }
}