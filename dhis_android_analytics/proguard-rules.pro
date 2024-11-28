# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Keep dhis2.org.analytics.charts.Charts
-keep class dhis2.org.analytics.charts.Charts { *; }

# Preserve attributes related to DataBinding
-keepattributes *Annotation*

-dontwarn org.dhis2.commons.bindings.CommonExtensionsKt
-dontwarn org.dhis2.commons.filters.data.FilterBindingsKt
-dontwarn org.dhis2.commons.resources.ColorType
-dontwarn org.dhis2.commons.resources.ColorUtils
