package com.qualcomm.audio.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.Gravity;
import android.graphics.Color;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Build UI programmatically - no XML layout needed
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.parseColor("#1a1a2e"));
        layout.setPadding(60, 60, 60, 60);

        TextView title = new TextView(this);
        title.setText("Qualcomm Audio 64");
        title.setTextSize(22f);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        subtitle.setText("Audio Routing Service");
        subtitle.setTextSize(13f);
        subtitle.setTextColor(Color.parseColor("#888888"));
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 8, 0, 48);

        TextView status = new TextView(this);
        status.setText("Service: Running");
        status.setTextSize(14f);
        status.setTextColor(Color.parseColor("#00ff88"));
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 0, 0, 32);

        Button btnStart = new Button(this);
        btnStart.setText("Start Service");
        btnStart.setOnClickListener(v -> {
            startForegroundService(new Intent(this, AudioRouterService.class));
            status.setText("Service: Running");
            status.setTextColor(Color.parseColor("#00ff88"));
        });

        Button btnStop = new Button(this);
        btnStop.setText("Stop Service");
        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, AudioRouterService.class));
            status.setText("Service: Stopped");
            status.setTextColor(Color.parseColor("#ff4444"));
        });

        layout.addView(title);
        layout.addView(subtitle);
        layout.addView(status);
        layout.addView(btnStart);
        layout.addView(btnStop);

        setContentView(layout);

        // Auto-start service on open
        startForegroundService(new Intent(this, AudioRouterService.class));
    }
}
