package com.qualcomm.audio.driver.a64;

import android.media.AudioAttributes;
import android.media.AudioManager;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    private static final String TARGET_PACKAGE = "com.roblox.client";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(TARGET_PACKAGE)) return;

        XposedHelpers.findAndHookMethod(
            AudioManager.class,
            "setMode",
            int.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    int mode = (int) param.args[0];
                    if (mode == AudioManager.MODE_IN_COMMUNICATION || mode == AudioManager.MODE_IN_CALL) {
                        param.args[0] = AudioManager.MODE_NORMAL;
                    }
                }
            }
        );

        XposedHelpers.findAndHookMethod(
            AudioAttributes.Builder.class,
            "setUsage",
            int.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    int usage = (int) param.args[0];
                    if (usage == AudioAttributes.USAGE_VOICE_COMMUNICATION) {
                        param.args[0] = AudioAttributes.USAGE_GAME;
                    }
                }
            }
        );
    }
}
