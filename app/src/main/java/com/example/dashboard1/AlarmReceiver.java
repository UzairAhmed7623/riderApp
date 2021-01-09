package com.example.dashboard1;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

    private static final int ALARM_ID = 1001;
    private static final long REPEAT_TIME = 10 * 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context,LocationService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_ID, i, PendingIntent.FLAG_CANCEL_CURRENT);
        ContextCompat.startForegroundService(context, i);

        Calendar cal = Calendar.getInstance();

//        //start 30 seconds after boot has completed
//        cal.add(Calendar.SECOND, 30);

        Log.d("Location", "This Alarm Started: \n" + Calendar.getInstance().getTime().toString());
        //fetch after every 10seconds
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), REPEAT_TIME, pendingIntent);
    }
}
