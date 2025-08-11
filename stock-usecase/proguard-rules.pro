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

# Keep runtime annotations for Hilt and Dagger
-keepattributes *Annotation*

# Keep ViewModels to ensure Hilt can reference them
-keep class  org.dhis2.android.rtsm.ui.managestock.ManageStockViewModel { *; }
-keep class  org.dhis2.android.rtsm.ui.home.HomeViewModel { *; }
-keep class  org.dhis2.android.rtsm.ui.base.BaseViewModel { *; }
-keep class org.dhis2.android.rtsm.services.SpeechRecognitionManager { *; }
-keep class org.dhis2.android.rtsm.services.rules.RuleValidationHelper { *; }
-keep class org.dhis2.android.rtsm.services.StockManager { *; }
-keep class org.dhis2.android.rtsm.services.MetadataManager { *; }
-keep class org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider { *; }
-keep class org.dhis2.android.rtsm.services.StockTableDimensionStore { *; }
-keep class org.dhis2.android.rtsm.ui.managestock.TableModelMapper { *; }

-dontwarn dhis2.org.analytics.charts.ui.GroupAnalyticsFragment$Companion
-dontwarn dhis2.org.analytics.charts.ui.GroupAnalyticsFragment
-dontwarn java.lang.invoke.StringConcatFactory
-dontwarn org.dhis2.commons.filters.FilterManager
-dontwarn org.dhis2.commons.orgunitselector.OUTreeFragment$Builder
-dontwarn org.dhis2.commons.orgunitselector.OUTreeFragment
-dontwarn org.dhis2.mobile.commons.orgunit.OrgUnitSelectorScope$ProgramCaptureScope
-dontwarn org.dhis2.mobile.commons.orgunit.OrgUnitSelectorScope
-dontwarn org.dhis2.commons.sync.OnDismissListener
-dontwarn org.dhis2.commons.sync.OnNoConnectionListener
-dontwarn org.dhis2.commons.sync.OnSyncNavigationListener
-dontwarn org.dhis2.commons.sync.SyncContext$TrackerProgram
-dontwarn org.dhis2.commons.sync.SyncContext
-dontwarn org.dhis2.commons.sync.SyncDialog
-dontwarn org.dhis2.composetable.TableConfigurationState
-dontwarn org.dhis2.composetable.TableScreenState
-dontwarn org.dhis2.composetable.TableState
-dontwarn org.dhis2.composetable.actions.TableResizeActions
-dontwarn org.dhis2.composetable.actions.Validator
-dontwarn org.dhis2.composetable.model.TableCell
-dontwarn org.dhis2.composetable.model.TextInputModel
-dontwarn org.dhis2.composetable.ui.DataSetTableScreenKt
-dontwarn org.dhis2.composetable.ui.TableColors
-dontwarn org.dhis2.composetable.ui.TableConfiguration
-dontwarn org.dhis2.composetable.ui.TableDimensions
-dontwarn org.dhis2.composetable.ui.TableThemeKt
-dontwarn org.dhis2.composetable.ui.semantics.TableSemanticsKt
-dontwarn org.dhis2.ui.buttons.FAButtonKt
-dontwarn org.dhis2.ui.dialogs.bottomsheet.BottomSheetDialog
-dontwarn org.dhis2.ui.dialogs.bottomsheet.BottomSheetDialogUiModel
-dontwarn org.dhis2.ui.dialogs.bottomsheet.DialogButtonStyle$DiscardButton
-dontwarn org.dhis2.ui.dialogs.bottomsheet.DialogButtonStyle$MainButton
-dontwarn org.dhis2.ui.dialogs.bottomsheet.DialogButtonStyle
-dontwarn org.dhis2.composetable.model.RowHeader
-dontwarn org.dhis2.composetable.model.TableHeader
-dontwarn org.dhis2.composetable.model.TableHeaderCell
-dontwarn org.dhis2.composetable.model.TableHeaderRow
-dontwarn org.dhis2.composetable.model.TableModel
-dontwarn org.dhis2.composetable.model.TableRowModel