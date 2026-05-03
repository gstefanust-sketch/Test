package com.qualcomm.audio.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.util.Log;

import java.io.DataOutputStream;

public class AudioRouterService extends Service {

    private static final String TAG = "QualcommAudioSvc";
    private static final String CHANNEL_ID = "qualcomm_audio_channel";
    private static final int NOTIF_ID = 1001;
    private static final int CHECK_INTERVAL_MS = 500;

    private AudioManager audioManager;
    private Handler handler;
    private BluetoothAdapter bluetoothAdapter;
    private boolean isRunning = false;

    // Monitor audio mode changes
    private final Runnable audioMonitor = new Runnable() {
        @Override
        public void run() {
            if (!isRunning) return;
            try {
                checkAndFixAudioRouting();
            } catch (Exception e) {
                Log.e(TAG, "Monitor error: " + e.getMessage());
            }
            handler.postDelayed(this, CHECK_INTERVAL_MS);
        }
    };

    // Listen for Bluetooth connection changes
    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action) ||
                BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                handler.postDelayed(() -> checkAndFixAudioRouting(), 1000);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        handler = new Handler(Looper.getMainLooper());
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIF_ID, buildNotification());
        
        // Register Bluetooth receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(btReceiver, filter);

        // Start monitoring
        isRunning = true;
        handler.post(audioMonitor);
        Log.d(TAG, "Service started");

        return START_STICKY;
    }

    private void checkAndFixAudioRouting() {
        int currentMode = audioManager.getMode();

        // Detect if communication mode is active (voice chat)
        boolean inCommunication = (currentMode == AudioManager.MODE_IN_COMMUNICATION ||
                                   currentMode == AudioManager.MODE_IN_CALL);

        if (!inCommunication) return;

        // Check if TWS/BT headset is connected
        boolean btA2dpConnected = isBtA2dpConnected();
        if (!btA2dpConnected) return;

        Log.d(TAG, "Communication mode detected with BT connected - fixing routing");

        // Fix 1: Force audio mode back to normal via root
        forceAudioModeNormal();

        // Fix 2: Force output to BT A2DP
        forceBluetoothA2dp();
    }

    private void forceAudioModeNormal() {
        try {
            // Use root to set audio mode via cmd
            String[] commands = {
                "cmd media_session volume --stream 0 --set 10",
                "setprop ro.audio.monitorOrientation false",
                "am broadcast -a android.media.AUDIO_BECOMING_NOISY"
            };
            executeRootCommands(commands);

            // Also try via AudioManager directly
            audioManager.setMode(AudioManager.MODE_NORMAL);
        } catch (Exception e) {
            Log.e(TAG, "forceAudioModeNormal error: " + e.getMessage());
        }
    }

    private void forceBluetoothA2dp() {
        try {
            // Force BT A2DP via root
            String[] commands = {
                "cmd bluetooth_manager enable",
                "service call audio 8 i32 1", // IBluetooth.startScoUsingVirtualVoiceCall
            };
            executeRootCommands(commands);

            // Try AudioManager approach
            if (audioManager.isBluetoothA2dpOn()) {
                audioManager.setBluetoothScoOn(false);
                // Force media to BT A2DP
                AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
                for (AudioDeviceInfo device : devices) {
                    if (device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP) {
                        audioManager.setCommunicationDevice(device);
                        Log.d(TAG, "Forced output to BT A2DP: " + device.getProductName());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "forceBluetoothA2dp error: " + e.getMessage());
        }
    }

    private boolean isBtA2dpConnected() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) return false;
        return bluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP)
               == BluetoothProfile.STATE_CONNECTED;
    }

    private void executeRootCommands(String[] commands) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            for (String cmd : commands) {
                os.writeBytes(cmd + "\n");
            }
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            Log.e(TAG, "Root command error: " + e.getMessage());
        }
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
            CHANNEL_ID,
            "Qualcomm Audio Service",
            NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Audio routing optimization service");
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(channel);
    }

    private Notification buildNotification() {
        return new Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Qualcomm Audio Driver")
            .setContentText("Audio routing active")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        handler.removeCallbacks(audioMonitor);
        try { unregisterReceiver(btReceiver); } catch (Exception ignored) {}
        super.onDestroy();
    }
}
