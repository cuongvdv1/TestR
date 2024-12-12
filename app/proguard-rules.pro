# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.util.**{ *; }
-keep class com.google.ads.**{*;}
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }
-ignorewarnings
# support-v7-appcompat
-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }
# support v4
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }

-dontwarn com.squareup.okhttp.**
-dontwarn okio.**
-keep class com.lib** {*;}
-keep class com.apiai** {*;}
-keep class com.util** {*;}
# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public class com.vm.backgroundremove.objectremove.** {*;}
# GSON Annotations
-keepclassmembers class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.firebase.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class org.koin.** { *; }

-keep class retrofit2.**{ *; }
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

-keepattributes EnclosingMethod
-keepclasseswithmembers class * {
    @retrofit2.* <methods>;
}

-keepclasseswithmembers interface * {
    @retrofit2.* <methods>;
}

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

