package com.example.dashboard1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;
import android.widget.Toast;

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
import java.util.List;
import java.util.Locale;

public class History extends AppCompatActivity {

    private CalendarView calHistoryView;
    private RecyclerView rvHistory;
    private FirebaseFirestore firebaseFirestore;
    private DocumentReference documentReference;
    private FirebaseAuth firebaseAuth;
    private ArrayList<String> docList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        calHistoryView = (CalendarView)findViewById(R.id.calHistoryView);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("user").document(firebaseAuth.getUid());

        ProgressDialog dialog = ProgressDialog.show(History.this, "Loading", "Please wait...", true);

        firebaseFirestore.collection("user").document(firebaseAuth.getUid()).collection("location").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        Log.d("TAG",queryDocumentSnapshot.getId());
                        docList.add(queryDocumentSnapshot.getId());
                        dialog.dismiss();
                    }
                }
                else {
                    Log.d("TAG",task.getException().toString());
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