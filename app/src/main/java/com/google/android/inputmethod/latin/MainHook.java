package com.google.android.inputmethod.latin;

import android.media.AudioAttributes;
import android.media.AudioManager;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    private static final String TAG = "RobloxAudioFix";
    private static final String TARGET_PACKAGE = "com.roblox.client";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(TARGET_PACKAGE)) return;

        XposedBridge.log(TAG + ": Hooking Roblox audio...");

        // Hook AudioManager.setMode() - prevents switching to MODE_IN_COMMUNICATION
        hookSetMode(lpparam);

        // Hook AudioAttributes.Builder.setUsage() - intercepts USAGE_VOICE_COMMUNICATION
        hookAudioAttributesBuilder(lpparam);

        // Hook AudioManager.requestAudioFocus() - intercepts voice communication focus
        hookRequestAudioFocus(lpparam);

        XposedBridge.log(TAG + ": Hooks installed successfully.");
    }

    private void hookSetMode(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                AudioManager.class,
                "setMode",
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        int mode = (int) param.args[0];
                        // MODE_IN_COMMUNICATION = 3, MODE_IN_CALL = 2
                        // Force to MODE_NORMAL = 0 so audio stays on media output (TWS A2DP)
                        if (mode == AudioManager.MODE_IN_COMMUNICATION || mode == AudioManager.MODE_IN_CALL) {
                            XposedBridge.log(TAG + ": Blocked setMode(" + mode + ") -> forcing MODE_NORMAL");
                            param.args[0] = AudioManager.MODE_NORMAL;
                        }
                    }
                }
            );
            XposedBridge.log(TAG + ": setMode hook installed");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": setMode hook failed: " + t.getMessage());
        }
    }

    private void hookAudioAttributesBuilder(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                AudioAttributes.Builder.class,
                "setUsage",
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        int usage = (int) param.args[0];
                        // USAGE_VOICE_COMMUNICATION = 2, USAGE_VOICE_COMMUNICATION_SIGNALLING = 3
                        // Replace with USAGE_GAME = 14 so system treats it as media/game
                        if (usage == AudioAttributes.USAGE_VOICE_COMMUNICATION) {
                            XposedBridge.log(TAG + ": Redirecting USAGE_VOICE_COMMUNICATION -> USAGE_GAME");
                            param.args[0] = AudioAttributes.USAGE_GAME;
                        }
                    }
                }
            );
            XposedBridge.log(TAG + ": AudioAttributes.Builder.setUsage hook installed");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": AudioAttributes hook failed: " + t.getMessage());
        }
    }

    private void hookRequestAudioFocus(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // Hook the modern AudioFocusRequest based requestAudioFocus
            Class<?> audioFocusRequestClass = XposedHelpers.findClass(
                "android.media.AudioFocusRequest", lpparam.classLoader
            );

            XposedHelpers.findAndHookMethod(
                AudioManager.class,
                "requestAudioFocus",
                audioFocusRequestClass,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log(TAG + ": requestAudioFocus intercepted");
                        // Let it through but setMode hook will handle the mode change
                    }
                }
            );
            XposedBridge.log(TAG + ": requestAudioFocus hook installed");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": requestAudioFocus hook failed: " + t.getMessage());
        }
    }
}
