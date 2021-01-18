package com.example.dashboard1;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

public class ProgressButton {

    private ProgressBar progressBar;
    private TextView tvLogin;

    ProgressButton(Context context, View view){
        progressBar = view.findViewById(R.id.progressBar);
        tvLogin = view.findViewById(R.id.tvLogin);

    }

    public void buttonActivated(){
        progressBar.setVisibility(View.VISIBLE);
        tvLogin.setText("Please wait...");
    }

    public void buttonFinished(){
        progressBar.setVisibility(View.GONE);
        tvLogin.setText("Try again!");
    }

    public void buttonFinishedSuccessfully(){
        progressBar.setVisibility(View.GONE);
        tvLogin.setText("Success");
    }

}
