-optimizationpasses 5
-dontpreverify
-repackageclasses 'q'
-allowaccessmodification
-renamesourcefileattribute ''
-keepattributes !SourceFile,!LineNumberTable

-keep public class com.qualcomm.audio.MainActivity
-keep public class com.qualcomm.audio.AudioService
-keep public class com.qualcomm.audio.BootReceiver

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}
