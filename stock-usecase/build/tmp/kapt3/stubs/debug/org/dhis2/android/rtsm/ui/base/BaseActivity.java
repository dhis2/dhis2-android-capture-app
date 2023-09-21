package org.dhis2.android.rtsm.ui.base;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.disposables.CompositeDisposable;
import org.dhis2.android.rtsm.R;
import org.dhis2.android.rtsm.commons.Constants;
import org.dhis2.android.rtsm.data.SpeechRecognitionState;
import org.dhis2.android.rtsm.data.TransactionType;
import org.dhis2.android.rtsm.utils.LocaleManager;
import timber.log.Timber;

/**
 * The base activity
 *
 * Sets the menu, and action bar.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u008c\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0011\n\u0000\n\u0002\u0010\u0015\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\b&\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0018H\u0014J\b\u0010\u0019\u001a\u00020\u0004H&J\u0010\u0010\u001a\u001a\u00020\u000e2\u0006\u0010\u0005\u001a\u00020\u0006H&J\u0016\u0010\u001b\u001a\u00020\u00162\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001fJ\u0017\u0010 \u001a\u0004\u0018\u00010\u001f2\u0006\u0010\r\u001a\u00020\u000eH\u0016\u00a2\u0006\u0002\u0010!J\n\u0010\"\u001a\u0004\u0018\u00010#H\u0016J\u0006\u0010$\u001a\u00020\u0004J\u001a\u0010%\u001a\u00020\u00162\u0006\u0010&\u001a\u00020\u001f2\b\u0010\'\u001a\u0004\u0018\u00010(H\u0016J\u0010\u0010)\u001a\u00020\u00102\u0006\u0010\r\u001a\u00020\u000eH\u0002J\u0012\u0010*\u001a\u00020\u00162\b\u0010+\u001a\u0004\u0018\u00010,H\u0014J\b\u0010-\u001a\u00020\u0016H\u0014J\u0010\u0010.\u001a\u00020\u00102\u0006\u0010/\u001a\u000200H\u0016J-\u00101\u001a\u00020\u00162\u0006\u00102\u001a\u00020\u001f2\u000e\u00103\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010(042\u0006\u00105\u001a\u000206H\u0016\u00a2\u0006\u0002\u00107J\b\u00108\u001a\u00020\u0016H\u0014J\b\u00109\u001a\u00020\u0016H\u0014J\b\u0010:\u001a\u00020\u0016H\u0016J \u0010;\u001a\u00020\u00162\f\u0010<\u001a\b\u0012\u0004\u0012\u00020>0=2\b\u0010\u0007\u001a\u0004\u0018\u00010\bH\u0016J\u000e\u0010?\u001a\u00020\u00162\u0006\u0010@\u001a\u00020AJ\u0010\u0010B\u001a\u00020\u00162\u0006\u0010C\u001a\u00020#H\u0002J\b\u0010D\u001a\u00020\u0010H\u0016J\b\u0010E\u001a\u00020\u0016H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\t\u0010\n\"\u0004\b\u000b\u0010\fR\u000e\u0010\r\u001a\u00020\u000eX\u0082.\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000f\u001a\u00020\u0010X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014\u00a8\u0006F"}, d2 = {"Lorg/dhis2/android/rtsm/ui/base/BaseActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "binding", "Landroidx/databinding/ViewDataBinding;", "disposable", "Lio/reactivex/disposables/CompositeDisposable;", "speechController", "Lorg/dhis2/android/rtsm/ui/base/SpeechController;", "getSpeechController", "()Lorg/dhis2/android/rtsm/ui/base/SpeechController;", "setSpeechController", "(Lorg/dhis2/android/rtsm/ui/base/SpeechController;)V", "viewModel", "Landroidx/lifecycle/ViewModel;", "voiceInputEnabled", "", "getVoiceInputEnabled", "()Z", "setVoiceInputEnabled", "(Z)V", "attachBaseContext", "", "newBase", "Landroid/content/Context;", "createViewBinding", "createViewModel", "displayError", "view", "Landroid/view/View;", "messageRes", "", "getCustomTheme", "(Landroidx/lifecycle/ViewModel;)Ljava/lang/Integer;", "getToolBar", "Landroidx/appcompat/widget/Toolbar;", "getViewBinding", "handleSpeechError", "code", "data", "", "isVoiceInputEnabled", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onOptionsItemSelected", "item", "Landroid/view/MenuItem;", "onRequestPermissionsResult", "requestCode", "permissions", "", "grantResults", "", "(I[Ljava/lang/String;[I)V", "onResume", "onStart", "onVoiceInputStateChanged", "registerSpeechRecognitionStatusObserver", "speechStatus", "Landroidx/lifecycle/LiveData;", "Lorg/dhis2/android/rtsm/data/SpeechRecognitionState;", "setTitle", "transactionType", "Lorg/dhis2/android/rtsm/data/TransactionType;", "setupToolbar", "toolbar", "showMoreOptions", "showPendingMessages", "psm-v2.9-DEV_debug"})
public abstract class BaseActivity extends androidx.appcompat.app.AppCompatActivity {
    private androidx.lifecycle.ViewModel viewModel;
    private androidx.databinding.ViewDataBinding binding;
    @org.jetbrains.annotations.Nullable
    private org.dhis2.android.rtsm.ui.base.SpeechController speechController;
    @org.jetbrains.annotations.NotNull
    private final io.reactivex.disposables.CompositeDisposable disposable = null;
    private boolean voiceInputEnabled = false;
    
    public BaseActivity() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final org.dhis2.android.rtsm.ui.base.SpeechController getSpeechController() {
        return null;
    }
    
    public final void setSpeechController(@org.jetbrains.annotations.Nullable
    org.dhis2.android.rtsm.ui.base.SpeechController p0) {
    }
    
    public final boolean getVoiceInputEnabled() {
        return false;
    }
    
    public final void setVoiceInputEnabled(boolean p0) {
    }
    
    @java.lang.Override
    protected void onCreate(@org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override
    protected void onStart() {
    }
    
    @java.lang.Override
    protected void onResume() {
    }
    
    /**
     * Should be overridden by subclasses that require custom logic
     */
    public void onVoiceInputStateChanged() {
    }
    
    private final boolean isVoiceInputEnabled(androidx.lifecycle.ViewModel viewModel) {
        return false;
    }
    
    @java.lang.Override
    protected void onDestroy() {
    }
    
    private final void showPendingMessages() {
    }
    
    @org.jetbrains.annotations.NotNull
    public abstract androidx.databinding.ViewDataBinding createViewBinding();
    
    /**
     * Initialize the ViewModel for this Activity
     */
    @org.jetbrains.annotations.NotNull
    public abstract androidx.lifecycle.ViewModel createViewModel(@org.jetbrains.annotations.NotNull
    io.reactivex.disposables.CompositeDisposable disposable);
    
    /**
     * Subclasses should override this to use a custom theme
     */
    @org.jetbrains.annotations.Nullable
    public java.lang.Integer getCustomTheme(@org.jetbrains.annotations.NotNull
    androidx.lifecycle.ViewModel viewModel) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.databinding.ViewDataBinding getViewBinding() {
        return null;
    }
    
    private final void setupToolbar(androidx.appcompat.widget.Toolbar toolbar) {
    }
    
    /**
     * Get the Activity's toolbar.
     * No toolbar is created by default. Subclasses should override this as necessary
     */
    @org.jetbrains.annotations.Nullable
    public androidx.appcompat.widget.Toolbar getToolBar() {
        return null;
    }
    
    /**
     * Indicates if the more options menu should be shown
     */
    public boolean showMoreOptions() {
        return false;
    }
    
    @java.lang.Override
    public boolean onOptionsItemSelected(@org.jetbrains.annotations.NotNull
    android.view.MenuItem item) {
        return false;
    }
    
    @java.lang.Override
    protected void attachBaseContext(@org.jetbrains.annotations.NotNull
    android.content.Context newBase) {
    }
    
    @java.lang.Override
    public void onRequestPermissionsResult(int requestCode, @org.jetbrains.annotations.NotNull
    java.lang.String[] permissions, @org.jetbrains.annotations.NotNull
    int[] grantResults) {
    }
    
    public void registerSpeechRecognitionStatusObserver(@org.jetbrains.annotations.NotNull
    androidx.lifecycle.LiveData<org.dhis2.android.rtsm.data.SpeechRecognitionState> speechStatus, @org.jetbrains.annotations.Nullable
    org.dhis2.android.rtsm.ui.base.SpeechController speechController) {
    }
    
    public void handleSpeechError(int code, @org.jetbrains.annotations.Nullable
    java.lang.String data) {
    }
    
    public final void displayError(@org.jetbrains.annotations.NotNull
    android.view.View view, int messageRes) {
    }
    
    public final void setTitle(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.TransactionType transactionType) {
    }
}