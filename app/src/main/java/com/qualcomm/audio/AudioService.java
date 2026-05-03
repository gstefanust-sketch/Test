package com.qualcomm.audio;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import java.io.DataOutputStream;

public class AudioService extends Service {

    private static final String CH = "qca";
    private static final int NID = 9001;
    private static final int INTERVAL = 500;

    private AudioManager am;
    private Handler handler;
    private boolean running = false;

    private final Runnable monitor = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            fixAudio();
            handler.postDelayed(this, INTERVAL);
        }
    };

    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent i) {
            handler.postDelayed(() -> fixAudio(), 800);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        handler = new Handler(Looper.getMainLooper());
        NotificationChannel ch = new NotificationChannel(CH, "Qualcomm Audio", NotificationManager.IMPORTANCE_MIN);
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification n = new Notification.Builder(this, CH)
            .setContentTitle("Qualcomm Audio Driver")
            .setContentText("Running")
            .setSmallIcon(android.R.drawable.stat_sys_headset)
            .setOngoing(true)
            .build();
        startForeground(NID, n);

        IntentFilter f = new IntentFilter();
        f.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        f.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(btReceiver, f);

        running = true;
        handler.post(monitor);
        return START_STICKY;
    }

    private void fixAudio() {
        int mode = am.getMode();
        boolean commMode = (mode == AudioManager.MODE_IN_COMMUNICATION || mode == AudioManager.MODE_IN_CALL);
        if (!commMode) return;

        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
        if (bt == null || !bt.isEnabled()) return;
        if (bt.getProfileConnectionState(BluetoothProfile.A2DP) != BluetoothProfile.STATE_CONNECTED) return;

        // Root: force audio mode normal
        runRoot(new String[]{
            "media volume --stream 3 --set 10",
            "media volume --stream 0 --set 7"
        });

        // Force output to A2DP
        am.setMode(AudioManager.MODE_NORMAL);
        am.setBluetoothScoOn(false);

        AudioDeviceInfo[] outputs = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
        for (AudioDeviceInfo d : outputs) {
            if (d.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP) {
                am.setCommunicationDevice(d);
                break;
            }
        }

        // Force mic to built-in
        AudioDeviceInfo[] inputs = am.getDevices(AudioManager.GET_DEVICES_INPUTS);
        for (AudioDeviceInfo d : inputs) {
            if (d.getType() == AudioDeviceInfo.TYPE_BUILTIN_MIC) {
                am.setPreferredMicrophoneDirection(AudioManager.MIC_DIRECTION_FRONT);
                break;
            }
        }
    }

    private void runRoot(String[] cmds) {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            for (String c : cmds) os.writeBytes(c + "\n");
            os.writeBytes("exit\n");
            os.flush();
            p.waitFor();
        } catch (Exception ignored) {}
    }

    @Override
    public IBinder onBind(Intent i) { return null; }

    @Override
    public void onDestroy() {
        running = false;
        handler.removeCallbacks(monitor);
        try { unregisterReceiver(btReceiver); } catch (Exception ignored) {}
        super.onDestroy();
    }
}
