package com.example.dashboard1;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

    private static final long REPEAT_TIME=1000*10;

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context,LocationService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1001, i, PendingIntent.FLAG_CANCEL_CURRENT);

        Calendar cal = Calendar.getInstance();

//        //start 30 seconds after boot has completed
//        cal.add(Calendar.SECOND, 30);

        //fetch after every 10seconds
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), REPEAT_TIME, pendingIntent);
    }
}
