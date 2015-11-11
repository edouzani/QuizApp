# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
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

-dontwarn org.apache.**
-dontwarn android.net.http.AndroidHttpClient
-dontwarn com.google.android.gms.**
-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.google.android.gms.** { *; }

-keep class org.apache.** { *; }
-keepattributes Signature

-dontwarn okio.**
-keepattributes SourceFile,LineNumberTable,*Annotation*
-keep class com.crashlytics.android.**
-keep class com.chartboost.** { *; }
-dontwarn android.support.**

-keep class com.almadev.znaniesila.events.NeedUpdateQuizesEvent {
     public <methods>;
 }
 -keep class com.almadev.znaniesila.events.QuizDownloadedEvent {
     public <methods>;
 }
 -keep class com.almadev.znaniesila.events.QuizesUpdateFailedEvent {
     public <methods>;
 }
  -keep class com.almadev.znaniesila.events.QuizesUpdateFinishedEvent {
      public <methods>;
  }

 -keepclasseswithmembers class * {
   public void onEvent(...);
 }
  -keepclasseswithmembers class * {
    public void onEventMainThread(...);
  }

  #Yandex metrica
  -keep class com.yandex.metrica.impl.* { *; }
  -dontwarn com.yandex.metrica.impl.*

  -keep class com.yandex.metrica.* { *; }

  -dontwarn com.yandex.metrica.*
