package org.dhis2.android.rtsm.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import org.dhis2.android.rtsm.R;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\u0018\u0000 \u00032\u00020\u0001:\u0001\u0003B\u0005\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0004"}, d2 = {"Lorg/dhis2/android/rtsm/utils/ActivityManager;", "", "()V", "Companion", "psm-v2.9-DEV_debug"})
public final class ActivityManager {
    @org.jetbrains.annotations.NotNull
    public static final org.dhis2.android.rtsm.utils.ActivityManager.Companion Companion = null;
    
    public ActivityManager() {
        super();
    }
    
    @kotlin.jvm.JvmStatic
    public static final void checkPermission(@org.jetbrains.annotations.NotNull
    android.app.Activity activity, int requestCode) {
    }
    
    @kotlin.jvm.JvmStatic
    public static final boolean hasFlash(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        return false;
    }
    
    @kotlin.jvm.JvmStatic
    public static final void showDialog(@org.jetbrains.annotations.NotNull
    android.content.Context context, int titleRes, @org.jetbrains.annotations.NotNull
    java.lang.String messageRes, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> confirmationCallback) {
    }
    
    @kotlin.jvm.JvmStatic
    public static final void showErrorMessage(@org.jetbrains.annotations.NotNull
    android.view.View view, @org.jetbrains.annotations.NotNull
    java.lang.String message) {
    }
    
    @kotlin.jvm.JvmStatic
    public static final void showInfoMessage(@org.jetbrains.annotations.NotNull
    android.view.View view, @org.jetbrains.annotations.NotNull
    java.lang.String message) {
    }
    
    @kotlin.jvm.JvmStatic
    private static final void showMessage(android.view.View view, java.lang.String message, boolean isError) {
    }
    
    @kotlin.jvm.JvmStatic
    public static final void showToast(@org.jetbrains.annotations.NotNull
    android.content.Context context, int messageRes) {
    }
    
    @kotlin.jvm.JvmStatic
    public static final void showToast(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    java.lang.String message) {
    }
    
    @kotlin.jvm.JvmStatic
    public static final void startActivity(@org.jetbrains.annotations.NotNull
    android.app.Activity activity, @org.jetbrains.annotations.NotNull
    android.content.Intent intent, boolean closeCurrentActivity) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0007J\u0010\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0007J.\u0010\r\u001a\u00020\u00042\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u000e\u001a\u00020\b2\u0006\u0010\u000f\u001a\u00020\u00102\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00040\u0012H\u0007J\u0018\u0010\u0013\u001a\u00020\u00042\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0010H\u0007J\u0018\u0010\u0017\u001a\u00020\u00042\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0010H\u0007J \u0010\u0018\u001a\u00020\u00042\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00102\u0006\u0010\u0019\u001a\u00020\nH\u0003J\u0018\u0010\u001a\u001a\u00020\u00042\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\bH\u0007J\u0018\u0010\u001a\u001a\u00020\u00042\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0016\u001a\u00020\u0010H\u0007J \u0010\u001b\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\nH\u0007\u00a8\u0006\u001f"}, d2 = {"Lorg/dhis2/android/rtsm/utils/ActivityManager$Companion;", "", "()V", "checkPermission", "", "activity", "Landroid/app/Activity;", "requestCode", "", "hasFlash", "", "context", "Landroid/content/Context;", "showDialog", "titleRes", "messageRes", "", "confirmationCallback", "Lkotlin/Function0;", "showErrorMessage", "view", "Landroid/view/View;", "message", "showInfoMessage", "showMessage", "isError", "showToast", "startActivity", "intent", "Landroid/content/Intent;", "closeCurrentActivity", "psm-v2.9-DEV_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @kotlin.jvm.JvmStatic
        public final void startActivity(@org.jetbrains.annotations.NotNull
        android.app.Activity activity, @org.jetbrains.annotations.NotNull
        android.content.Intent intent, boolean closeCurrentActivity) {
        }
        
        @kotlin.jvm.JvmStatic
        private final void showMessage(android.view.View view, java.lang.String message, boolean isError) {
        }
        
        @kotlin.jvm.JvmStatic
        public final void showErrorMessage(@org.jetbrains.annotations.NotNull
        android.view.View view, @org.jetbrains.annotations.NotNull
        java.lang.String message) {
        }
        
        @kotlin.jvm.JvmStatic
        public final void showInfoMessage(@org.jetbrains.annotations.NotNull
        android.view.View view, @org.jetbrains.annotations.NotNull
        java.lang.String message) {
        }
        
        @kotlin.jvm.JvmStatic
        public final void showToast(@org.jetbrains.annotations.NotNull
        android.content.Context context, int messageRes) {
        }
        
        @kotlin.jvm.JvmStatic
        public final void showToast(@org.jetbrains.annotations.NotNull
        android.content.Context context, @org.jetbrains.annotations.NotNull
        java.lang.String message) {
        }
        
        @kotlin.jvm.JvmStatic
        public final boolean hasFlash(@org.jetbrains.annotations.NotNull
        android.content.Context context) {
            return false;
        }
        
        @kotlin.jvm.JvmStatic
        public final void showDialog(@org.jetbrains.annotations.NotNull
        android.content.Context context, int titleRes, @org.jetbrains.annotations.NotNull
        java.lang.String messageRes, @org.jetbrains.annotations.NotNull
        kotlin.jvm.functions.Function0<kotlin.Unit> confirmationCallback) {
        }
        
        @kotlin.jvm.JvmStatic
        public final void checkPermission(@org.jetbrains.annotations.NotNull
        android.app.Activity activity, int requestCode) {
        }
    }
}