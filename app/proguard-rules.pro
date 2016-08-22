# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Applications/Android Studio.app/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

## Branch SDK
-keep class com.google.android.gms.ads.identifier.** { *; }

## MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }
-dontwarn io.realm.**

## Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

## Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

## GSON
-keepattributes Signature
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class ch.giantific.qwittig.data.rest.ExchangeRatesResult { *; }
-keep class ch.giantific.qwittig.presentation.stats.models.** { *; }

## Parse
-keepattributes *Annotation*
-keepattributes Signature
-dontwarn android.net.SSLCertificateSocketFactory
-dontwarn android.app.Notification
-dontwarn com.squareup.**
-dontwarn okio.**

## Apache Commons Math
-dontwarn org.apache.commons.math3.geometry.**