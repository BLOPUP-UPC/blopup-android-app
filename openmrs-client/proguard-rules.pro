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
# Optimization options

-keepattributes *Annotation*, Signature, Exception, SourceFile, LineNumberTable
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

# for DexGuard only
# -keepresourcexmlelements manifest/application/meta-data@value=GlideModule
-dontwarn javax.annotation.ParametersAreNonnullByDefault
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.**
-dontnote retrofit2.Platform
-dontwarn retrofit2.Platform$Java8
-dontwarn org.joda.convert.*

# Keep classes and methods from your app that are referenced by the Android framework
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends androidx.appcompat.app.ActionBar
-keep public class * extends androidx.appcompat.app.AppCompatActivity
-keep public class * extends androidx.appcompat.app.ToolbarActionBar

-keep public class * extends java.lang.Exception
# Keep classes and methods from your app that are referenced by the Android framework
-keep public class * extends androidx.core.* {
}
-keep public class * extends androidx.appcompat.* {
}
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.content.Context {
   public void *(android.view.View);
   public void *(android.view.MenuItem);
}

-keep public class * extends androidx.fragment.app.* {
    public protected *;
}

-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

 # Keep any classes or methods that are used for network operations (e.g. Retrofit)
-keepclasseswithmembers class * {
    @retrofit2.* <methods>;
}
-keepclasseswithmembers interface * {
    @retrofit2.* <methods>;
}
 -keepclassmembers class rx.internal.util.unsafe.*SpscArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
 }
 -keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
     rx.internal.util.atomic.LinkedQueueNode producerNode;
 }
 -keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
     rx.internal.util.atomic.LinkedQueueNode consumerNode;
 }
 -dontnote rx.internal.util.PlatformDependent


# Keep any classes or methods that are used for database operations (e.g. Room)
-keep class edu.upc.sdk.library.databases.** { *; }
-keepclasseswithmembers class edu.upc.sdk.library.databases.** { *; }

-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

-keep class edu.upc.sdk.library.models.** { *; }
-keepclasseswithmembers class edu.upc.sdk.library.models.** { *; }

# Keep any classes or methods that are used for dependency injection (e.g. Dagger)
-keep class edu.upc.openmrs.di.** { *; }
-keepclasseswithmembers class edu.upc.openmrs.di.** { *; }

# Keep any classes that are used for crashlytics
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

# Remove logging statements
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** e(...);
}

