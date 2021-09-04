package com.broderickwestrope.whiteboard.exams.Listeners;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.broderickwestrope.whiteboard.exams.ExamReminderManager;

public class ExamReminderReceiver extends BroadcastReceiver {
    public static String CHANNEL_ID = "Exam Reminder";

    @Override
    public void onReceive(Context context, Intent intent) {
//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        Notification notification = intent.getParcelableExtra("notification"); // THis is the actual notification witht he content and style that we constructed
//        assert notificationManager != null;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            int importance = NotificationManager.IMPORTANCE_HIGH;
//            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
//            notificationManager.createNotificationChannel(notificationChannel);
//        }
//        // This prefix (427) is to ensure we don't have two of the same notification. The value is "exam" converted to ASCII values and added together
//        int id = 4270000 + intent.getIntExtra("id", 0);
//        notificationManager.notify(id, notification);

        ExamReminderManager notificationHelper = new ExamReminderManager(context);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification();
        notificationHelper.getManager().notify(1, nb.build());
    }

}