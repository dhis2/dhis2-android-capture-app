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