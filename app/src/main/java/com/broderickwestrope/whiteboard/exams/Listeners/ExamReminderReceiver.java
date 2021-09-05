package com.broderickwestrope.whiteboard.exams.Listeners;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.broderickwestrope.whiteboard.exams.ExamReminderManager;

public class ExamReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ExamReminderManager notificationHelper = new ExamReminderManager(context);
        Bundle b = intent.getExtras();
        Notification n = b.getParcelable("notification");
        notificationHelper.getManager().notify(getUniqueNID(context), n);

        Toast.makeText(context, "Reminder Received", Toast.LENGTH_SHORT).show();
    }

    // Returns a unique notification ID
    public int getUniqueNID(Context context) {

        int NOTIFICATION_ID_UPPER_LIMIT = 30000; // Arbitrary number.

        int NOTIFICATION_ID_LOWER_LIMIT = 0;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int previousTokenId = sharedPreferences.getInt("currentNotificationTokenId", 0);

        int currentTokenId = previousTokenId + 1;

        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (currentTokenId < NOTIFICATION_ID_UPPER_LIMIT) {

            editor.putInt("currentNotificationTokenId", currentTokenId); // }
        } else {
            //If reaches the limit reset to lower limit..
            editor.putInt("currentNotificationTokenId", NOTIFICATION_ID_LOWER_LIMIT);
        }

        editor.commit();

        return currentTokenId;
    }

}