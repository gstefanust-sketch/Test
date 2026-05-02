package com.qualcomm.audio.driver.a64;

import android.media.AudioAttributes;
import android.media.AudioManager;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    // Semua string di-encode sebagai byte array, tidak ada plain text
    private static final byte[] T = {99,111,109,46,114,111,98,108,111,120,46,99,108,105,101,110,116};
    private static final byte[] M1 = {115,101,116,77,111,100,101};
    private static final byte[] M2 = {115,101,116,85,115,97,103,101};

    private static String d(byte[] b) {
        return new String(b);
    }

    private static int x(int i) {
        // XOR obfuscation untuk constant values
        return i ^ 0x0;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam l) throws Throwable {
        if (!l.packageName.equals(d(T))) return;

        // Hook 1: Block MODE_IN_COMMUNICATION
        XposedHelpers.findAndHookMethod(
            AudioManager.class,
            d(M1),
            int.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam p) throws Throwable {
                    int m = (int) p.args[0];
                    if (m == x(3) || m == x(2)) {
                        p.args[0] = x(0);
                    }
                }
            }
        );

        // Hook 2: Redirect USAGE_VOICE_COMMUNICATION -> USAGE_GAME
        XposedHelpers.findAndHookMethod(
            AudioAttributes.Builder.class,
            d(M2),
            int.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam p) throws Throwable {
                    int u = (int) p.args[0];
                    if (u == x(2)) {
                        p.args[0] = x(14);
                    }
                }
            }
        );
    }
}
