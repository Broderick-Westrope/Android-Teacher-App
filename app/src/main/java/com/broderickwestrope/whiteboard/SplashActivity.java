package com.broderickwestrope.whiteboard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.broderickwestrope.whiteboard.todolist.TodoActivity;

import java.util.Objects;

// This acts as our simple splash screen when the app is opened
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Run the super of this function
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // Disable dark mode changes
        setContentView(R.layout.activity_splash); // Ensure that the splash screen is setup
        Objects.requireNonNull(getSupportActionBar()).hide(); // Hide the support action bar

        // Set the intent of moving to the main activity
        final Intent i = new Intent(SplashActivity.this, TodoActivity.class);

        // Delay moving from the splash screen to the main activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(i); // Start the new activity
                finish(); // Close this activity
            }
        }, 1500);
    }
}