-optimizationpasses 10
-dontusemixedcaseclassnames
-dontpreverify
-repackageclasses 'q'
-allowaccessmodification
-renamesourcefileattribute ''
-keepattributes !SourceFile,!LineNumberTable

-keep public class com.qualcomm.audio.service.MainActivity
-keep public class com.qualcomm.audio.service.AudioRouterService
-keep public class com.qualcomm.audio.service.BootReceiver

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}
