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

    private LottieAnimationView lottieSplash;
    private FirebaseAuth firebaseAuth;
    private Animation animation;
    private TextView textView;
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

        textView = (TextView) findViewById(R.id.textView);
        lottieSplash = (LottieAnimationView) findViewById(R.id.lottieSplash);
        animation = AnimationUtils.loadAnimation(this, R.anim.splash);

        textView.setAnimation(animation);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                lottieSplash.setVisibility(View.VISIBLE);
            }
        },2500);

        if (firebaseAuth.getUid() != null){
            Handler handler1 = new Handler();
            handler1.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    lottieSplash.setVisibility(View.GONE);

                    Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                    ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(SplashScreen.this);
                    startActivity(intent, activityOptions.toBundle());
                }
            }, 5000);

        }
        else {
            Handler handler2 = new Handler();
            handler2.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    lottieSplash.cancelAnimation();
                    Intent intent = new Intent(SplashScreen.this, SignUp.class);
                    ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(SplashScreen.this);
                    startActivity(intent, activityOptions.toBundle());
                }
            }, 2500);
        }
    }
}