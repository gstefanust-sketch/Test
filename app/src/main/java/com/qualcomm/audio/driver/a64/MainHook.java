package com.qualcomm.audio.driver.a64;

import java.lang.reflect.Method;

public class MainHook implements de.robv.android.xposed.IXposedHookLoadPackage {

    private static String s(int[] c) {
        char[] r = new char[c.length];
        for (int i = 0; i < c.length; i++) r[i] = (char)(c[i] ^ 0x5A);
        return new String(r);
    }

    // "com.roblox.client" XOR 0x5A
    private static final int[] T = {57,22,23,84,40,22,23,54,22,23,84,57,54,19,8,15,30};
    // "setMode" XOR 0x5A
    private static final int[] M1 = {41,15,30,87,23,22,31};
    // "setUsage" XOR 0x5A
    private static final int[] M2 = {41,15,30,95,41,10,23,31};
    // "AudioManager" XOR 0x5A  
    private static final int[] C1 = {27,30,23,22,27,87,23,10,15,10,23,31};
    // "AudioAttributes$Builder" XOR 0x5A
    private static final int[] C2 = {27,30,23,22,27,27,30,30,40,22,30,30,95,84,30,30,54,23,40};

    @Override
    public void handleLoadPackage(de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam l) throws Throwable {
        if (!l.packageName.equals(s(T))) return;

        de.robv.android.xposed.XposedHelpers.findAndHookMethod(
            android.media.AudioManager.class,
            s(M1),
            int.class,
            new de.robv.android.xposed.XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam p) throws Throwable {
                    int m = (int) p.args[0];
                    if (m == (0xF ^ 0xC) || m == (0xF ^ 0xD)) {
                        p.args[0] = (0xF ^ 0xF);
                    }
                }
            }
        );

        de.robv.android.xposed.XposedHelpers.findAndHookMethod(
            android.media.AudioAttributes.Builder.class,
            s(M2),
            int.class,
            new de.robv.android.xposed.XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam p) throws Throwable {
                    int u = (int) p.args[0];
                    if (u == (0xF ^ 0xD)) {
                        p.args[0] = (0xF ^ 0x1);
                    }
                }
            }
        );
    }
}
