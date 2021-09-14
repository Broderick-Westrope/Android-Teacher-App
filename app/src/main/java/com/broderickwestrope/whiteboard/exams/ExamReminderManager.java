package com.broderickwestrope.whiteboard.exams;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.broderickwestrope.whiteboard.Interfaces.ExamReminderReceiver;
import com.broderickwestrope.whiteboard.Models.ExamModel;
import com.broderickwestrope.whiteboard.R;
import com.broderickwestrope.whiteboard.student_records.RecordsActivity;

import java.util.Calendar;

// Class to handle our notifications for reminding users they have an upcoming exam
public class ExamReminderManager extends ContextWrapper {
    public static final String channelID = "examReminderChannelID"; // The ID of the channel we use for the notifications
    public static final String channelName = "Exam Reminders"; // The name of the channel we use for the notifications
    Context context; // The context from which the class is being used
    private NotificationManager notificationManager; // Handles notifying the user

    // Class constructor
    public ExamReminderManager(Context context) {
        super(context);
        this.context = context; // Set the context the class is being used in
        // If we are using a device with high enough version for notification channels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(); // Create a new channel
        }
    }

    // Create a new channel
    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        // Create the notification channel using the global variables and giving it high importance
        NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
        getManager().createNotificationChannel(channel); // Get the manager and submit the channel for creation
    }

    // Get the notification manager
    public NotificationManager getManager() {
        if (notificationManager == null) // If it doesnt exist, create one
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager; // Return the notification manager
    }

    // Set an exam reminder
    public void setReminder(Calendar c, ExamModel exam) {
        // Create the notification object
        Notification notification = getNotification(exam.getName(), exam.getUnit(), exam.getDate(), exam.getTime());

        // Create a new alarm manager (this is how we handle setting the alarm for the future)
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ExamReminderReceiver.class); // Create an intent for receiving the notification
        Bundle b = new Bundle(); // Create a bundle to hold the extras
        b.putParcelable("notification", notification); // Store the notification object as a parcelable in the bundle
        intent.putExtras(b); // Put the extras in the intent
        // Create a new pending intent (ie. an intent for future use)
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, 0);

        // Set the notification for an exact point in time (given in milliseconds).
        // We use RTC_WAKEUP to turn the screen on when the phone is asleep on notification
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);

        // Display that the notification was set successfully
        Toast.makeText(context, "Reminder set for " + exam.getName(), Toast.LENGTH_LONG).show();
    }


    // Creates a notification object (using NotificationCompat.Builder) for an exam with the given values
    private Notification getNotification(String name, String unit, String date, String time) {
        // This schedules a notification when the exam is edited using our function to determine when
        String smallContent = "Don't forget about \"" + name + "\" for " + unit;
        String bigContent = "Don't forget that \"" + name + "\" for " + unit + " is coming up on " + time + " on " + date + ".";

        Intent clickIntent = new Intent(context, RecordsActivity.class); // New intent for where want to take the user when they press the notification
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Helps preserve the user's expected navigation experience after they open the app via the notification
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, clickIntent, 0);

        //Create a new notification builder. Here, the channelId is how the users can decide to mute or un-mute certain notifications (ie. leave on exam notifications but remove push notifications)
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID)
                .setContentTitle("Exam Reminder") // Set the title of the notification
                .setContentText(smallContent) // Set the content for when the notification is small.
                .setSmallIcon(R.mipmap.applogo) // Set the icon to be used on the notification
                .setAutoCancel(true) //? Not sure if we need this. Cancels the notification when pressed...
                .setGroup(channelID) //? groups all exam notifications together so they can be collapsed
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigContent)) // Set the content for when the notification is expanded.
                .setContentIntent(pendingIntent) // Set the intent. This makes it so that when the notification is pressed, the student records page is opened
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER);
        return builder.build(); // Return the built notification object
    }
}
