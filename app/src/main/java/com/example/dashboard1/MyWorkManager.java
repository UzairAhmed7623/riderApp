package com.example.dashboard1;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.dashboard1.utilities.Notification;

import java.util.List;

public class MyWorkManager extends Worker {

    public MyWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        //only if the notification setting is on will the wake up service starts(can be set periodic without this condition)
        if (isServiceRunning(getApplicationContext(), "LocationService")) {
            return Result.success();
        }
        Intent intent = new Intent(getApplicationContext(), LocationService.class);
        intent.setAction("android.intent.action.RESPOND_VIA_MESSAGE");
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getApplicationContext().startForegroundService(intent);
            } else {
                getApplicationContext().startService(intent);
            }
        } catch (Exception e) {
            return Result.failure();
        }
        return Result.success();
    }

    private boolean isServiceRunning(Context context, String serviceName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            return false;
        }
        List<ActivityManager.RunningServiceInfo> infos = activityManager.getRunningServices(100);
        for (ActivityManager.RunningServiceInfo info : infos) {
            String className = info.service.getClassName();
            if (serviceName.equals(className))
                return true;
        }
        return false;
    }
}