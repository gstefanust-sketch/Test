package com.qualcomm.audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent i) {
        ctx.startForegroundService(new Intent(ctx, AudioService.class));
    }
}
