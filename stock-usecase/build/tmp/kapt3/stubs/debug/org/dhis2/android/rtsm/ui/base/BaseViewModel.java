package org.dhis2.android.rtsm.ui.base;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.disposables.Disposable;
import org.dhis2.android.rtsm.data.AppConfig;
import org.dhis2.android.rtsm.data.RowAction;
import org.dhis2.android.rtsm.data.models.Transaction;
import org.dhis2.android.rtsm.services.rules.RuleValidationHelper;
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\b\u0017\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J.\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0017J\u000e\u0010\u0018\u001a\u00020\u00072\u0006\u0010\u0019\u001a\u00020\u0013J\u0006\u0010\u001a\u001a\u00020\u001bR\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00070\t8F\u00a2\u0006\u0006\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u001c"}, d2 = {"Lorg/dhis2/android/rtsm/ui/base/BaseViewModel;", "Landroidx/lifecycle/ViewModel;", "schedulerProvider", "Lorg/dhis2/android/rtsm/services/scheduler/BaseSchedulerProvider;", "(Lorg/dhis2/android/rtsm/services/scheduler/BaseSchedulerProvider;)V", "_showGuide", "Landroidx/lifecycle/MutableLiveData;", "", "showGuide", "Landroidx/lifecycle/LiveData;", "getShowGuide", "()Landroidx/lifecycle/LiveData;", "evaluate", "Lio/reactivex/disposables/Disposable;", "ruleValidationHelper", "Lorg/dhis2/android/rtsm/services/rules/RuleValidationHelper;", "action", "Lorg/dhis2/android/rtsm/data/RowAction;", "program", "", "transaction", "Lorg/dhis2/android/rtsm/data/models/Transaction;", "appConfig", "Lorg/dhis2/android/rtsm/data/AppConfig;", "isVoiceInputEnabled", "prefKey", "toggleGuideDisplay", "", "psm-v2.9-DEV_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel
public class BaseViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull
    private final org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider schedulerProvider = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<java.lang.Boolean> _showGuide = null;
    
    @javax.inject.Inject
    public BaseViewModel(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider schedulerProvider) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.LiveData<java.lang.Boolean> getShowGuide() {
        return null;
    }
    
    /**
     * Evaluates the quantity assigned to the StockItem
     *
     * @param action The row action that comprises the item, adapter position, quantity and
     * callback invoked when the validation completes
     */
    @org.jetbrains.annotations.NotNull
    public final io.reactivex.disposables.Disposable evaluate(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.services.rules.RuleValidationHelper ruleValidationHelper, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.RowAction action, @org.jetbrains.annotations.NotNull
    java.lang.String program, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.models.Transaction transaction, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.AppConfig appConfig) {
        return null;
    }
    
    public final void toggleGuideDisplay() {
    }
    
    public final boolean isVoiceInputEnabled(@org.jetbrains.annotations.NotNull
    java.lang.String prefKey) {
        return false;
    }
}