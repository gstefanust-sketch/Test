-optimizationpasses 10
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# Repack all classes into single package with short name
-repackageclasses 'q'
-allowaccessmodification
-mergeinterfacesaggressively

# Remove all debug info
-renamesourcefileattribute ''
-keepattributes !SourceFile,!LineNumberTable,!LocalVariable*,!Signature,!Annotation*

# Remove all logging
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Keep only entry point
-keep public class com.qualcomm.audio.driver.a64.MainHook {
    public void handleLoadPackage(de.robv.android.xposed.callbacks.XC_LoadPackage$LoadPackageParam);
}

# Keep Xposed interfaces (required)
-keep interface de.robv.android.xposed.** { *; }
-keep class de.robv.android.xposed.** { *; }
