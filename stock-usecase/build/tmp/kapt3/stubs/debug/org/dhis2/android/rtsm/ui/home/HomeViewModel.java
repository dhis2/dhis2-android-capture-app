package org.dhis2.android.rtsm.ui.home;

import androidx.lifecycle.SavedStateHandle;
import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.disposables.CompositeDisposable;
import kotlinx.coroutines.flow.StateFlow;
import org.dhis2.android.rtsm.R;
import org.dhis2.android.rtsm.data.AppConfig;
import org.dhis2.android.rtsm.data.OperationState;
import org.dhis2.android.rtsm.data.TransactionType;
import org.dhis2.android.rtsm.data.models.Transaction;
import org.dhis2.android.rtsm.exceptions.InitializationException;
import org.dhis2.android.rtsm.exceptions.UserIntentParcelCreationException;
import org.dhis2.android.rtsm.services.MetadataManager;
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider;
import org.dhis2.android.rtsm.ui.base.BaseViewModel;
import org.dhis2.android.rtsm.ui.home.model.SettingsUiState;
import org.dhis2.android.rtsm.utils.ParcelUtils;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000n\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u0001B\'\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\r\u0010\u001e\u001a\u0004\u0018\u00010\u001f\u00a2\u0006\u0002\u0010 J\u0006\u0010!\u001a\u00020\"J\b\u0010#\u001a\u00020$H\u0002J\b\u0010%\u001a\u00020$H\u0002J\u0006\u0010&\u001a\u00020$J\u000e\u0010\'\u001a\u00020$2\u0006\u0010(\u001a\u00020)J\u0010\u0010*\u001a\u00020$2\b\u0010+\u001a\u0004\u0018\u00010\u000fJ\u000e\u0010,\u001a\u00020$2\u0006\u0010-\u001a\u00020\u0011R \u0010\u000b\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000e0\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R \u0010\u0010\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110\u000e0\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00130\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082\u0004\u00a2\u0006\u0002\n\u0000R#\u0010\u0016\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000e0\r0\u00178F\u00a2\u0006\u0006\u001a\u0004\b\u0018\u0010\u0019R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R#\u0010\u001a\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110\u000e0\r0\u00178F\u00a2\u0006\u0006\u001a\u0004\b\u001b\u0010\u0019R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00130\u0017\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u0019\u00a8\u0006."}, d2 = {"Lorg/dhis2/android/rtsm/ui/home/HomeViewModel;", "Lorg/dhis2/android/rtsm/ui/base/BaseViewModel;", "disposable", "Lio/reactivex/disposables/CompositeDisposable;", "schedulerProvider", "Lorg/dhis2/android/rtsm/services/scheduler/BaseSchedulerProvider;", "metadataManager", "Lorg/dhis2/android/rtsm/services/MetadataManager;", "savedState", "Landroidx/lifecycle/SavedStateHandle;", "(Lio/reactivex/disposables/CompositeDisposable;Lorg/dhis2/android/rtsm/services/scheduler/BaseSchedulerProvider;Lorg/dhis2/android/rtsm/services/MetadataManager;Landroidx/lifecycle/SavedStateHandle;)V", "_destinations", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lorg/dhis2/android/rtsm/data/OperationState;", "", "Lorg/hisp/dhis/android/core/option/Option;", "_facilities", "Lorg/hisp/dhis/android/core/organisationunit/OrganisationUnit;", "_settingsUiSate", "Lorg/dhis2/android/rtsm/ui/home/model/SettingsUiState;", "config", "Lorg/dhis2/android/rtsm/data/AppConfig;", "destinationsList", "Lkotlinx/coroutines/flow/StateFlow;", "getDestinationsList", "()Lkotlinx/coroutines/flow/StateFlow;", "facilities", "getFacilities", "settingsUiState", "getSettingsUiState", "checkForFieldErrors", "", "()Ljava/lang/Integer;", "getData", "Lorg/dhis2/android/rtsm/data/models/Transaction;", "loadDestinations", "", "loadFacilities", "resetSettings", "selectTransaction", "type", "Lorg/dhis2/android/rtsm/data/TransactionType;", "setDestination", "destination", "setFacility", "facility", "psm-v2.9-DEV_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel
public final class HomeViewModel extends org.dhis2.android.rtsm.ui.base.BaseViewModel {
    @org.jetbrains.annotations.NotNull
    private final io.reactivex.disposables.CompositeDisposable disposable = null;
    @org.jetbrains.annotations.NotNull
    private final org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider schedulerProvider = null;
    @org.jetbrains.annotations.NotNull
    private final org.dhis2.android.rtsm.services.MetadataManager metadataManager = null;
    @org.jetbrains.annotations.NotNull
    private final org.dhis2.android.rtsm.data.AppConfig config = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<org.dhis2.android.rtsm.data.OperationState<java.util.List<org.hisp.dhis.android.core.organisationunit.OrganisationUnit>>> _facilities = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<org.dhis2.android.rtsm.data.OperationState<java.util.List<org.hisp.dhis.android.core.option.Option>>> _destinations = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<org.dhis2.android.rtsm.ui.home.model.SettingsUiState> _settingsUiSate = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<org.dhis2.android.rtsm.ui.home.model.SettingsUiState> settingsUiState = null;
    
    @javax.inject.Inject
    public HomeViewModel(@org.jetbrains.annotations.NotNull
    io.reactivex.disposables.CompositeDisposable disposable, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider schedulerProvider, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.services.MetadataManager metadataManager, @org.jetbrains.annotations.NotNull
    androidx.lifecycle.SavedStateHandle savedState) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<org.dhis2.android.rtsm.data.OperationState<java.util.List<org.hisp.dhis.android.core.organisationunit.OrganisationUnit>>> getFacilities() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<org.dhis2.android.rtsm.data.OperationState<java.util.List<org.hisp.dhis.android.core.option.Option>>> getDestinationsList() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<org.dhis2.android.rtsm.ui.home.model.SettingsUiState> getSettingsUiState() {
        return null;
    }
    
    private final void loadDestinations() {
    }
    
    private final void loadFacilities() {
    }
    
    public final void selectTransaction(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.TransactionType type) {
    }
    
    public final void setFacility(@org.jetbrains.annotations.NotNull
    org.hisp.dhis.android.core.organisationunit.OrganisationUnit facility) {
    }
    
    public final void setDestination(@org.jetbrains.annotations.Nullable
    org.hisp.dhis.android.core.option.Option destination) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer checkForFieldErrors() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final org.dhis2.android.rtsm.data.models.Transaction getData() {
        return null;
    }
    
    public final void resetSettings() {
    }
}