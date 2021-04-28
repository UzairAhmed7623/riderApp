package com.inkhornsolutions.riderapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class History extends AppCompatActivity {

    private CalendarView calHistoryView;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private ArrayList<String> docList = new ArrayList<>();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#02AA4E")));
        actionBar.setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);

        calHistoryView = (CalendarView) findViewById(R.id.calHistoryView);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.progress_layout);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        firebaseFirestore.collection("Users").document(firebaseAuth.getUid()).collection("location").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        Log.d("TAG", queryDocumentSnapshot.getId());
                        docList.add(queryDocumentSnapshot.getId());

                        progressDialog.dismiss();

                        setCalHistoryView();
                    }
                }
                else {

                    progressDialog.dismiss();

                    Log.d("TAG", task.getException().toString());
                    Toast.makeText(History.this, task.getException().toString(), Toast.LENGTH_LONG);

                }
            }
        });
    }

    public void setCalHistoryView() {
        calHistoryView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {

                String date = getDateFormat(year, month, dayOfMonth);
                Log.d("LocationDate1", date);

                boolean isEquals = docList.contains(date);

                if (isEquals) {
                    Log.d("LocationDate1", date);
                    Intent intent = new Intent(History.this, ViewHistoryOnMap.class);
                    intent.putExtra("doc", date);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else {
                    Log.d("LocationDate1", "Not matched");
                    Toast.makeText(History.this, "No data found!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public String getDateFormat(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(year, month, dayOfMonth);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String date = simpleDateFormat.format(calendar.getTime());
        return date;
    }

}