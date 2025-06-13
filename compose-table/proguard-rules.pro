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


# Keep KeyboardInputType and its nested classes
-keep class org.dhis2.composetable.model.KeyboardInputType { *; }
-keep class org.dhis2.composetable.model.KeyboardInputType$* { *; }

# Keep ValidationResult and its nested classes
-keep class org.dhis2.composetable.model.ValidationResult { *; }
-keep class org.dhis2.composetable.model.ValidationResult$* { *; }

-dontwarn java.lang.invoke.StringConcatFactory