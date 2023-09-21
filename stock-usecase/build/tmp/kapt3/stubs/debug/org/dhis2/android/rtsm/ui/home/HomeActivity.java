package org.dhis2.android.rtsm.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.material.ScaffoldState;
import androidx.compose.ui.Modifier;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;
import dagger.hilt.android.AndroidEntryPoint;
import org.dhis2.android.rtsm.R;
import org.dhis2.android.rtsm.data.AppConfig;
import org.dhis2.android.rtsm.data.TransactionType;
import org.dhis2.android.rtsm.ui.managestock.ManageStockViewModel;
import org.dhis2.android.rtsm.utils.NetworkUtils;
import org.dhis2.commons.filters.FilterManager;
import org.dhis2.commons.sync.OnDismissListener;
import org.dhis2.commons.sync.OnSyncNavigationListener;
import org.dhis2.commons.sync.SyncContext;
import org.dhis2.commons.sync.SyncDialog;
import org.dhis2.commons.sync.SyncStatusItem;

@dagger.hilt.android.AndroidEntryPoint
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000d\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0015\u001a\u00020\u0016H\u0002J\u0012\u0010\u0017\u001a\u00020\u00162\b\u0010\u0018\u001a\u0004\u0018\u00010\u0019H\u0014J\u0010\u0010\u001a\u001a\u00020\u00162\u0006\u0010\u001b\u001a\u00020\u001cH\u0002J \u0010\u001d\u001a\u00020\u00162\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020#H\u0002J \u0010$\u001a\u00020\u00162\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020!2\u0006\u0010%\u001a\u00020#H\u0002J\u0010\u0010&\u001a\u00020\u00162\u0006\u0010\'\u001a\u00020(H\u0002R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082.\u00a2\u0006\u0002\n\u0000R\u001b\u0010\b\u001a\u00020\t8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\f\u0010\r\u001a\u0004\b\n\u0010\u000bR\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0010\u001a\u00020\u00118BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0014\u0010\r\u001a\u0004\b\u0012\u0010\u0013\u00a8\u0006)"}, d2 = {"Lorg/dhis2/android/rtsm/ui/home/HomeActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "barcodeLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "Lcom/journeyapps/barcodescanner/ScanOptions;", "filterManager", "Lorg/dhis2/commons/filters/FilterManager;", "manageStockViewModel", "Lorg/dhis2/android/rtsm/ui/managestock/ManageStockViewModel;", "getManageStockViewModel", "()Lorg/dhis2/android/rtsm/ui/managestock/ManageStockViewModel;", "manageStockViewModel$delegate", "Lkotlin/Lazy;", "themeColor", "", "viewModel", "Lorg/dhis2/android/rtsm/ui/home/HomeViewModel;", "getViewModel", "()Lorg/dhis2/android/rtsm/ui/home/HomeViewModel;", "viewModel$delegate", "configureScanner", "", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onScanCompleted", "result", "Lcom/journeyapps/barcodescanner/ScanIntentResult;", "showSnackBar", "scope", "Lkotlinx/coroutines/CoroutineScope;", "scaffoldState", "Landroidx/compose/material/ScaffoldState;", "message", "", "synchronizeData", "programUid", "updateTheme", "type", "Lorg/dhis2/android/rtsm/data/TransactionType;", "psm-v2.9-DEV_debug"})
public final class HomeActivity extends androidx.appcompat.app.AppCompatActivity {
    @org.jetbrains.annotations.NotNull
    private final kotlin.Lazy viewModel$delegate = null;
    @org.jetbrains.annotations.NotNull
    private final kotlin.Lazy manageStockViewModel$delegate = null;
    private int themeColor;
    private org.dhis2.commons.filters.FilterManager filterManager;
    private androidx.activity.result.ActivityResultLauncher<com.journeyapps.barcodescanner.ScanOptions> barcodeLauncher;
    
    public HomeActivity() {
        super();
    }
    
    private final org.dhis2.android.rtsm.ui.home.HomeViewModel getViewModel() {
        return null;
    }
    
    private final org.dhis2.android.rtsm.ui.managestock.ManageStockViewModel getManageStockViewModel() {
        return null;
    }
    
    @java.lang.Override
    protected void onCreate(@org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
    }
    
    private final void updateTheme(org.dhis2.android.rtsm.data.TransactionType type) {
    }
    
    private final void synchronizeData(kotlinx.coroutines.CoroutineScope scope, androidx.compose.material.ScaffoldState scaffoldState, java.lang.String programUid) {
    }
    
    private final void showSnackBar(kotlinx.coroutines.CoroutineScope scope, androidx.compose.material.ScaffoldState scaffoldState, java.lang.String message) {
    }
    
    private final void configureScanner() {
    }
    
    private final void onScanCompleted(com.journeyapps.barcodescanner.ScanIntentResult result) {
    }
}