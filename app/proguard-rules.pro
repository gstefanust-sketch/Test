-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keep public class com.qualcomm.audio.driver.a64.MainHook

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

-repackageclasses 'com.qualcomm.internal'
-allowaccessmodification
