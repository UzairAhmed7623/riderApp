package com.example.dashboard1;

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
import androidx.annotation.UiThread;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

public class History extends AppCompatActivity {

    private CalendarView calHistoryView;
    private RecyclerView rvHistory;
    private FirebaseFirestore firebaseFirestore;
    private DocumentReference documentReference;
    private FirebaseAuth firebaseAuth;
    private ArrayList<String> docList = new ArrayList<>();
    private LottieAnimationView lottieHistory;
    private LinearLayout lottieLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#02AA4E")));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lottieLayout = (LinearLayout) findViewById(R.id.lottieLayout);
        lottieHistory = (LottieAnimationView) findViewById(R.id.lottieHistory);
        calHistoryView = (CalendarView)findViewById(R.id.calHistoryView);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("user").document(firebaseAuth.getUid());

        lottieHistory.setVisibility(View.VISIBLE);
        lottieLayout.setVisibility(View.VISIBLE);




        firebaseFirestore.collection("Users").document(firebaseAuth.getUid()).collection("location").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        Log.d("TAG",queryDocumentSnapshot.getId());
                        docList.add(queryDocumentSnapshot.getId());
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                lottieLayout.setVisibility(View.GONE);
                                lottieHistory.setVisibility(View.GONE);
                            }
                        },2500);
                    }
                }
                else {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            lottieLayout.setVisibility(View.GONE);
                            lottieHistory.setVisibility(View.GONE);
                        }
                    },2500);

                    Log.d("TAG",task.getException().toString());
                    Toast.makeText(History.this, task.getException().toString(), Toast.LENGTH_LONG);

                }
            }
        });

        calHistoryView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {

                String date = getDateFormat(year,month,dayOfMonth);
                Log.d("LocationDate1",date);

                boolean isEquals = docList.contains(date);

                if (isEquals){
                    Log.d("LocationDate1", date);
                    Intent intent = new Intent(History.this, ViewHistoryOnMap.class);
                    intent.putExtra("doc", date);
                    startActivity(intent);
                }
                else {
                    Log.d("LocationDate1","Not matched");
                    Toast.makeText(History.this, "No data found!", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public String getDateFormat(int year, int month, int dayOfMonth){
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(year, month, dayOfMonth);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String date = simpleDateFormat.format(calendar.getTime());
        return date;
    }

}