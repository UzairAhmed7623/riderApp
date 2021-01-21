package com.example.dashboard1;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends AppCompatActivity {

    private LottieAnimationView lottieSplash;
    private FirebaseAuth firebaseAuth;
    private boolean mShouldFinishOnStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mShouldFinishOnStop = false;

        getSupportActionBar().hide();

        firebaseAuth = FirebaseAuth.getInstance();

        lottieSplash = (LottieAnimationView) findViewById(R.id.lottieSplash);

        lottieSplash.playAnimation();

        if (firebaseAuth.getUid() != null){
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    lottieSplash.cancelAnimation();
                    Intent intent = new Intent(SplashScreen.this, LoginActivity.class);

                    ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(SplashScreen.this);
                    startActivity(intent, activityOptions.toBundle());
                    mShouldFinishOnStop = true;
                }
            }, 2500);

        }
        else {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    lottieSplash.cancelAnimation();
                    Intent intent = new Intent(SplashScreen.this, SignUp.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }, 2500);
        }



    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mShouldFinishOnStop) finish();
    }
}