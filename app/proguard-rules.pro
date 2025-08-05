# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\ppajuelo\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.
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

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Keep Hilt-generated components and entry points
-keep class dagger.** { *; }
-keep class hilt.** { *; }
-keep class dagger.hilt.** { *; }
-keep class androidx.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class dagger.internal.** { *; }
-keep class dagger.multibindings.** { *; }
-keep interface dagger.hilt.EntryPoint
-keep interface dagger.hilt.InstallIn
-keep @dagger.hilt.components.SingletonComponent class *
# Keep DataBinding classes
-keep class androidx.databinding.** { *; }
-keep class **.databinding.** { *; }

# Keep @Provides, @Binds, and other annotations
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

-dontwarn autovalue.shaded.com.google$.errorprone.annotations.$CanIgnoreReturnValue
-dontwarn autovalue.shaded.com.google$.errorprone.annotations.concurrent.$LazyInit
-dontwarn com.android.org.conscrypt.SSLParametersImpl
-dontwarn java.lang.management.ManagementFactory
-dontwarn javax.lang.model.SourceVersion
-dontwarn javax.lang.model.element.Element
-dontwarn javax.lang.model.element.Modifier
-dontwarn javax.lang.model.type.TypeMirror
-dontwarn javax.lang.model.type.TypeVisitor
-dontwarn javax.lang.model.util.SimpleTypeVisitor7
-dontwarn javax.management.InstanceAlreadyExistsException
-dontwarn javax.management.MBeanServer
-dontwarn javax.management.ObjectInstance
-dontwarn javax.management.ObjectName
-dontwarn javax.naming.Context
-dontwarn javax.naming.InitialContext
-dontwarn javax.naming.NameNotFoundException
-dontwarn javax.naming.NoInitialContextException
-dontwarn org.apache.harmony.xnet.provider.jsse.SSLParametersImpl
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.dhis2.tracker.NavigationBarUIState
-dontwarn org.dhis2.tracker.TEIDashboardItems
-dontwarn org.dhis2.tracker.data.ProfilePictureProvider
-dontwarn org.dhis2.tracker.events.CreateEventUseCase
-dontwarn org.dhis2.tracker.events.CreateEventUseCaseRepository
-dontwarn org.dhis2.tracker.relationships.data.EventRelationshipsRepository
-dontwarn org.dhis2.tracker.relationships.data.RelationshipsRepository
-dontwarn org.dhis2.tracker.relationships.data.TrackerRelationshipsRepository
-dontwarn org.dhis2.tracker.relationships.domain.DeleteRelationships
-dontwarn org.dhis2.tracker.relationships.domain.GetRelationshipsByType
-dontwarn org.dhis2.tracker.relationships.model.ListSelectionState
-dontwarn org.dhis2.tracker.relationships.model.RelationshipDirection
-dontwarn org.dhis2.tracker.relationships.model.RelationshipItem
-dontwarn org.dhis2.tracker.relationships.model.RelationshipModel
-dontwarn org.dhis2.tracker.relationships.model.RelationshipOwnerType
-dontwarn org.dhis2.tracker.relationships.model.RelationshipSection
-dontwarn org.dhis2.tracker.relationships.model.RelationshipTopBarIconState$List
-dontwarn org.dhis2.tracker.relationships.model.RelationshipTopBarIconState$Map
-dontwarn org.dhis2.tracker.relationships.model.RelationshipTopBarIconState$Selecting
-dontwarn org.dhis2.tracker.relationships.model.RelationshipTopBarIconState
-dontwarn org.dhis2.tracker.relationships.ui.RelationshipsScreenKt
-dontwarn org.dhis2.tracker.relationships.ui.RelationshipsUiState$Success
-dontwarn org.dhis2.tracker.relationships.ui.RelationshipsUiState
-dontwarn org.dhis2.tracker.relationships.ui.RelationshipsViewModel
-dontwarn org.dhis2.tracker.ui.AvatarProvider
-dontwarn org.joda.convert.FromString
-dontwarn org.joda.convert.ToString
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE
-dontwarn org.slf4j.impl.StaticLoggerBinder

#AndroidX
-dontwarn com.google.android.material.**
-keep class com.google.android.material.** { *; }

-dontwarn androidx.**
-keep class androidx.** { *; }
-keep interface androidx.* { *; }

#Data binding
-keep class * extends androidx.databinding.DataBinderMapper { *; }
-dontwarn androidx.databinding.**
-keep class androidx.databinding.** { *; }
-keep class * extends androidx.databinding.DataBinderMapper

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

### Gson ProGuard and R8 rules which are relevant for all users
### This file is automatically recognized by ProGuard and R8, see https://developer.android.com/build/shrink-code#configuration-files
###
### IMPORTANT:
### - These rules are additive; don't include anything here which is not specific to Gson (such as completely
###   disabling obfuscation for all classes); the user would be unable to disable that then
### - These rules are not complete; users will most likely have to add additional rules for their specific
###   classes, for example to disable obfuscation for certain fields or to keep no-args constructors
###

# Keep generic signatures; needed for correct type resolution
-keepattributes Signature

# Keep Gson annotations
# Note: Cannot perform finer selection here to only cover Gson annotations, see also https://stackoverflow.com/q/47515093
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

### The following rules are needed for R8 in "full mode" which only adheres to `-keepattribtues` if
### the corresponding class or field is matches by a `-keep` rule as well, see
### https://r8.googlesource.com/r8/+/refs/heads/main/compatibility-faq.md#r8-full-mode

# Keep class TypeToken (respectively its generic signature) if present
-if class com.google.gson.reflect.TypeToken
-keep,allowobfuscation class com.google.gson.reflect.TypeToken

# Keep any (anonymous) classes extending TypeToken
-keep,allowobfuscation class * extends com.google.gson.reflect.TypeToken

# Keep classes with @JsonAdapter annotation
-keep,allowobfuscation,allowoptimization @com.google.gson.annotations.JsonAdapter class *

# Keep fields with any other Gson annotation
# Also allow obfuscation, assuming that users will additionally use @SerializedName or
# other means to preserve the field names
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.Expose <fields>;
  @com.google.gson.annotations.JsonAdapter <fields>;
  @com.google.gson.annotations.Since <fields>;
  @com.google.gson.annotations.Until <fields>;
}

# Keep no-args constructor of classes which can be used with @JsonAdapter
# By default their no-args constructor is invoked to create an adapter instance
-keepclassmembers class * extends com.google.gson.TypeAdapter {
  <init>();
}
-keepclassmembers class * implements com.google.gson.TypeAdapterFactory {
  <init>();
}
-keepclassmembers class * implements com.google.gson.JsonSerializer {
  <init>();
}
-keepclassmembers class * implements com.google.gson.JsonDeserializer {
  <init>();
}

# Keep fields annotated with @SerializedName for classes which are referenced.
# If classes with fields annotated with @SerializedName have a no-args
# constructor keep that as well. Based on
# https://issuetracker.google.com/issues/150189783#comment11.
# See also https://github.com/google/gson/pull/2420#discussion_r1241813541
# for a more detailed explanation.
-if class *
-keepclasseswithmembers,allowobfuscation class <1> {
  @com.google.gson.annotations.SerializedName <fields>;
}
-if class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers,allowobfuscation,allowoptimization class <1> {
  <init>();
}

-dontwarn java.lang.invoke.StringConcatFactory

# Preserve Kotlin data classes (general rule for data classes)
-keepclasseswithmembers class * {
    kotlin.Metadata *;
}

# Keep the Uri class from Android SDK (this is generally safe)
-keep class android.net.Uri {
    <init>(...);
    public static android.net.Uri parse(java.lang.String);
    public *;
}

# Jackson
-keep @com.fasterxml.jackson.annotation.JsonIgnoreProperties class * { *; }
-keep class com.fasterxml.** { *; }
-keep class org.codehaus.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepclassmembers public final enum com.fasterxml.jackson.annotation.JsonAutoDetect$Visibility {
    public static final com.fasterxml.jackson.annotation.JsonAutoDetect$Visibility *;
}

# General
-keepattributes SourceFile,LineNumberTable,*Annotation*,EnclosingMethod,Signature,Exceptions,InnerClasses

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn java.beans.ConstructorProperties
-dontwarn java.beans.Transient

#-keep class org.dhis2.usescases.login.auth.AuthServiceModel
-dontwarn org.hisp.dhis.**
-keep class org.hisp.dhis.** {*;}
-dontwarn org.cache2k.**
-keep class org.cache2k.** {*;}

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn javax.management.InstanceNotFoundException

-keep class org.dhis2.maps.** { *; }
-keep interface org.dhis2.maps.** { *; }
-keep enum org.dhis2.maps.** { *; }