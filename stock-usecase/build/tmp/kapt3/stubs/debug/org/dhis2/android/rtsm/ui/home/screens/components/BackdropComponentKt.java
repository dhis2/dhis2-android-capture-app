package org.dhis2.android.rtsm.ui.home.screens.components;

import android.annotation.SuppressLint;
import android.app.Activity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.compose.material.BackdropValue;
import androidx.compose.material.ExperimentalMaterialApi;
import androidx.compose.material.ScaffoldState;
import androidx.compose.material.SnackbarDuration;
import androidx.compose.material.SnackbarResult;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Modifier;
import androidx.fragment.app.FragmentManager;
import com.journeyapps.barcodescanner.ScanOptions;
import org.dhis2.android.rtsm.R;
import org.dhis2.android.rtsm.data.TransactionType;
import org.dhis2.android.rtsm.ui.home.HomeViewModel;
import org.dhis2.android.rtsm.ui.home.model.DataEntryStep;
import org.dhis2.android.rtsm.ui.home.model.EditionDialogResult;
import org.dhis2.android.rtsm.ui.managestock.ManageStockViewModel;
import org.dhis2.ui.dialogs.bottomsheet.BottomSheetDialog;
import org.dhis2.ui.dialogs.bottomsheet.BottomSheetDialogUiModel;
import org.dhis2.ui.dialogs.bottomsheet.DialogButtonStyle;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\\\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a\u0097\u0001\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00100\u000f2\u0006\u0010\u0011\u001a\u00020\u001228\b\u0002\u0010\u0013\u001a2\u0012\u0013\u0012\u00110\u0015\u00a2\u0006\f\b\u0016\u0012\b\b\u0017\u0012\u0004\b\b(\u0018\u0012\u0013\u0012\u00110\u0012\u00a2\u0006\f\b\u0016\u0012\b\b\u0017\u0012\u0004\b\b(\u0011\u0012\u0004\u0012\u00020\u00010\u0014H\u0007\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0019\u0010\u001a\u001a\u0018\u0010\u001b\u001a\u00020\u00012\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\u0011\u001a\u00020\u0012H\u0007\u001a<\u0010\u001c\u001a\u00020\u00012\u0006\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020\u001e2\u0006\u0010\f\u001a\u00020\r2\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00010!2\f\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00010!H\u0002\u0082\u0002\u000b\n\u0005\b\u00a1\u001e0\u0001\n\u0002\b\u0019\u00a8\u0006#"}, d2 = {"Backdrop", "", "activity", "Landroid/app/Activity;", "viewModel", "Lorg/dhis2/android/rtsm/ui/home/HomeViewModel;", "manageStockViewModel", "Lorg/dhis2/android/rtsm/ui/managestock/ManageStockViewModel;", "modifier", "Landroidx/compose/ui/Modifier;", "themeColor", "Landroidx/compose/ui/graphics/Color;", "supportFragmentManager", "Landroidx/fragment/app/FragmentManager;", "barcodeLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "Lcom/journeyapps/barcodescanner/ScanOptions;", "scaffoldState", "Landroidx/compose/material/ScaffoldState;", "syncAction", "Lkotlin/Function2;", "Lkotlinx/coroutines/CoroutineScope;", "Lkotlin/ParameterName;", "name", "scope", "Backdrop-FHprtrg", "(Landroid/app/Activity;Lorg/dhis2/android/rtsm/ui/home/HomeViewModel;Lorg/dhis2/android/rtsm/ui/managestock/ManageStockViewModel;Landroidx/compose/ui/Modifier;JLandroidx/fragment/app/FragmentManager;Landroidx/activity/result/ActivityResultLauncher;Landroidx/compose/material/ScaffoldState;Lkotlin/jvm/functions/Function2;)V", "DisplaySnackBar", "launchBottomSheet", "title", "", "subtitle", "onDiscard", "Lkotlin/Function0;", "onKeepEdition", "psm-v2.9-DEV_debug"})
public final class BackdropComponentKt {
    
    private static final void launchBottomSheet(java.lang.String title, java.lang.String subtitle, androidx.fragment.app.FragmentManager supportFragmentManager, kotlin.jvm.functions.Function0<kotlin.Unit> onDiscard, kotlin.jvm.functions.Function0<kotlin.Unit> onKeepEdition) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void DisplaySnackBar(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.ui.managestock.ManageStockViewModel manageStockViewModel, @org.jetbrains.annotations.NotNull
    androidx.compose.material.ScaffoldState scaffoldState) {
    }
}