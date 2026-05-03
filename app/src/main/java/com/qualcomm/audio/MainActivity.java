package com.qualcomm.audio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Qualcomm Audio Driver\nv3.6.4\n\nService Active");
        tv.setTextColor(Color.WHITE);
        tv.setBackgroundColor(Color.parseColor("#0d0d1a"));
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(18f);
        setContentView(tv);
        startForegroundService(new Intent(this, AudioService.class));
        finish(); // close immediately after starting service
    }
}
