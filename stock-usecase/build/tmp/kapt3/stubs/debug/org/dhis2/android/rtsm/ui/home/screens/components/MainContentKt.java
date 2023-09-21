package org.dhis2.android.rtsm.ui.home.screens.components;

import androidx.activity.result.ActivityResultLauncher;
import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.foundation.text.KeyboardOptions;
import androidx.compose.material.BackdropScaffoldState;
import androidx.compose.material.ExperimentalMaterialApi;
import androidx.compose.material.TextFieldDefaults;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.input.ImeAction;
import androidx.compose.ui.text.input.KeyboardType;
import com.journeyapps.barcodescanner.ScanOptions;
import org.dhis2.android.rtsm.R;
import org.dhis2.android.rtsm.data.TransactionType;
import org.dhis2.android.rtsm.ui.home.HomeViewModel;
import org.dhis2.android.rtsm.ui.home.model.DataEntryStep;
import org.dhis2.android.rtsm.ui.home.model.SettingsUiState;
import org.dhis2.android.rtsm.ui.managestock.ManageStockViewModel;
import org.dhis2.android.rtsm.ui.scanner.ScannerActivity;
import org.dhis2.composetable.actions.TableResizeActions;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00008\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\u001aM\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\b\u0010\u0004\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\rH\u0007\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u000f\u0010\u0010\u001a\u0016\u0010\u0011\u001a\u00020\u00012\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u000e0\rH\u0002\u001a\u0010\u0010\u0013\u001a\u00020\u00052\u0006\u0010\u0014\u001a\u00020\u0015H\u0002\u0082\u0002\u000b\n\u0005\b\u00a1\u001e0\u0001\n\u0002\b\u0019\u00a8\u0006\u0016"}, d2 = {"MainContent", "", "backdropState", "Landroidx/compose/material/BackdropScaffoldState;", "isFrontLayerDisabled", "", "themeColor", "Landroidx/compose/ui/graphics/Color;", "viewModel", "Lorg/dhis2/android/rtsm/ui/home/HomeViewModel;", "manageStockViewModel", "Lorg/dhis2/android/rtsm/ui/managestock/ManageStockViewModel;", "barcodeLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "Lcom/journeyapps/barcodescanner/ScanOptions;", "MainContent-FNF3uiM", "(Landroidx/compose/material/BackdropScaffoldState;Ljava/lang/Boolean;JLorg/dhis2/android/rtsm/ui/home/HomeViewModel;Lorg/dhis2/android/rtsm/ui/managestock/ManageStockViewModel;Landroidx/activity/result/ActivityResultLauncher;)V", "scanBarcode", "launcher", "shouldDisplayTable", "settingsUiState", "Lorg/dhis2/android/rtsm/ui/home/model/SettingsUiState;", "psm-v2.9-DEV_debug"})
public final class MainContentKt {
    
    private static final boolean shouldDisplayTable(org.dhis2.android.rtsm.ui.home.model.SettingsUiState settingsUiState) {
        return false;
    }
    
    private static final void scanBarcode(androidx.activity.result.ActivityResultLauncher<com.journeyapps.barcodescanner.ScanOptions> launcher) {
    }
}