package com.broderickwestrope.whiteboard.exams;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.broderickwestrope.whiteboard.R;

public class ExamReminderManager extends ContextWrapper {
    public static final String channelID = "channelID";
    public static final String channelName = "Channel Name";
    //    ExamReminderReceiver receiver;
    Context context;
    private NotificationManager mManager;

    public ExamReminderManager(Context base) {
        super(base);
        context = base;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);

        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return mManager;
    }

    public NotificationCompat.Builder getChannelNotification() {
        return new NotificationCompat.Builder(getApplicationContext(), channelID)
                .setContentTitle("Alarm!")
                .setContentText("Your AlarmManager is working.")
                .setSmallIcon(R.mipmap.applogo);
    }
//
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    public void examNotificaton(String name, String unit, String date, String time, int id) {
//
//        // TODO Comment this better/more
//        // This schedules a notification when the exam is edited using our function to determine when
//        String smallContent = "Don't forget about \"" + name + "\" for " + unit;
//        String bigContent = "Don't forget that \"" + name + "\" for " + unit + " is coming up on " + time + " on " + date + ".";
//        Notification notification = createNotification(smallContent, bigContent);
//        scheduleNotification(notification, date, time, id);
//    }
//
//
//    // Creates a notification (using NotificationCompat.Builder) for an exam with the given text.
//    private Notification createNotification(String smallContent, String bigContent) {
//        Intent clickIntent = new Intent(context, RecordsActivity.class); // New intent for where want to take the user when they press the notification
//        clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Helps preserve the user's expected navigation experience after they open the app via the notification
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, clickIntent, 0);
//
//        //Create a new notification builder. Here, the channelId is how the users can decide to mute or un-mute certain notifications (ie. leave on exam notifications but remove push notifications)
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ExamReminderReceiver.CHANNEL_ID)
//                .setContentTitle("Exam Reminder") // Set the title of the notification
//                .setContentText(smallContent) // Set the content for when the notification is small.
//                .setSmallIcon(R.mipmap.applogo) // Set the icon to be used on the notification
//                .setAutoCancel(true) //? Not sure if we need this. Cancels the notification when pressed...
//                .setGroup(ExamReminderReceiver.CHANNEL_ID) //? groups all exam notifications together so they can be collapsed
//                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigContent)) // Set the content for when the notification is expanded.
//                .setContentIntent(pendingIntent) // Set the intent. This makes it so that when the notification is pressed, the student records page is opened
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setCategory(NotificationCompat.CATEGORY_REMINDER);
//        return builder.build(); // Build the notification object
//    }
//
//
//    // Schedules the given exam notification for before the given date and time.
//    //TODO Make it so that in the settings they can set how early before they want to be notified
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    private void scheduleNotification(Notification notification, String date, String time, int id) {
//        // Combine the date and time values and calculate the delay of the notification
//        long delay = calculateDelay(date + " " + time);
//
//        if (delay <= 0) { // Ensures there is some form of positive delay on the notification (ie. the exam is in the future)
//            return;
//        }
//        Toast.makeText(context, "Delay: " + String.valueOf(delay / 1000) + " Seconds.", Toast.LENGTH_SHORT).show();
//
//        // Create intent to call our custom exam reminder class (which makes performs the reminder)
//        Intent notificationIntent = new Intent(context, ExamReminderReceiver.class); // Create a new intent to call our exam reminder class
//        //! THis is the value i believe
//        notificationIntent.putExtra("id", id); // Add the notification ID
//        notificationIntent.putExtra("notification", notification); // Add the notification
//
//        //Create a pending intent. This is how we grant the application permission to send a notification even when the app is closed
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, notificationIntent, 0);//PendingIntent.FLAG_UPDATE_CURRENT);
//
//        // Get an alarm manager and set the pending intent (essentially telling it to notify us on the given delay)
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        assert alarmManager != null; //Make sure that the alarm manager is not null
//        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, delay, pendingIntent);
////        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, delay, pendingIntent); // Schedule the notification
//    }
//
//
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    private long calculateDelay(String dateTime) {
//        // Get the current date and time
//        Date c = Calendar.getInstance().getTime();
//
//        // Format the current date and time to the correct format
//        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
//        String currentTime = df.format(c);
//        long delay = 0;
//
//        try {
//            // Turn the dates from strings to the Date type
//            Date currentDate = df.parse(currentTime);
//            Date examStart = df.parse(dateTime);
//            return examStart.getTime();
//
//            // Get the different between the two times, giving us the time till the exam starts. Negative means that the exam has begun. These times are stored as the amount of milliseconds.
////            delay = examStart.getTime() - currentDate.getTime();
////            delay -= 0 * 1000; //TODO Change this to minus the users preferred value (in settings). This determines how early to schedule the reminder
//        } catch (Exception exception) {
//            Toast.makeText(context, "ERROR: Unable to find difference in dates.", Toast.LENGTH_SHORT).show();
//        }
//        return delay;
//    }
}
