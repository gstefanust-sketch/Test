package com.qualcomm.audio.driver.a64;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    // Decode XOR encoded strings
    private static String x(int[] c) {
        char[] r = new char[c.length];
        for (int i = 0; i < c.length; i++) r[i] = (char)(c[i] ^ 0x37);
        return new String(r);
    }

    // "com.roblox.client" XOR 0x37
    private static final int[] P = {84,90,87,17,67,90,87,83,90,87,17,84,83,126,114,121,103};
    // "setMode" XOR 0x37
    private static final int[] A = {68,82,71,26,90,87,82};
    // "setUsage" XOR 0x37
    private static final int[] B = {68,82,71,102,68,119,85,82};
    // "android" XOR 0x37
    private static final int[] C = {86,87,71,84,90,87,71};

    // Obfuscated constants
    private static final int MODE_NORMAL = 0x1 ^ 0x1;           // 0
    private static final int MODE_IN_CALL = 0x3 ^ 0x1;          // 2
    private static final int MODE_IN_COMM = 0x2 ^ 0x1;          // 3
    private static final int USAGE_VOICE = 0x7 ^ 0x5;           // 2
    private static final int USAGE_GAME = 0xB ^ 0x5;            // 14

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam l) throws Throwable {
        // Only hook system_server and android processes
        String pkg = l.packageName;
        if (!pkg.equals(x(C)) && !pkg.equals("system")) return;

        hookAudioMode(l);
        hookAudioUsage(l);
    }

    private void hookAudioMode(final XC_LoadPackage.LoadPackageParam l) {
        try {
            XposedHelpers.findAndHookMethod(
                android.media.AudioManager.class,
                x(A),
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam p) throws Throwable {
                        int mode = (int) p.args[0];
                        if (mode == MODE_IN_CALL || mode == MODE_IN_COMM) {
                            p.args[0] = MODE_NORMAL;
                        }
                    }
                }
            );
        } catch (Throwable ignored) {}
    }

    private void hookAudioUsage(final XC_LoadPackage.LoadPackageParam l) {
        try {
            XposedHelpers.findAndHookMethod(
                android.media.AudioAttributes.Builder.class,
                x(B),
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam p) throws Throwable {
                        int usage = (int) p.args[0];
                        if (usage == USAGE_VOICE) {
                            p.args[0] = USAGE_GAME;
                        }
                    }
                }
            );
        } catch (Throwable ignored) {}
    }
}
