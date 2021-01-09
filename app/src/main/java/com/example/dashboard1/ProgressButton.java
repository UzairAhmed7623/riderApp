package com.example.dashboard1;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

public class ProgressButton {

    private CardView cardView;
    private ConstraintLayout constraintLayout;
    private ProgressBar progressBar;
    private TextView tvLogin;
    Animation fadein;
    ProgressButton(Context context, View view){
        cardView = view.findViewById(R.id.cardView);
        constraintLayout = view.findViewById(R.id.constraintLayout);
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

}
