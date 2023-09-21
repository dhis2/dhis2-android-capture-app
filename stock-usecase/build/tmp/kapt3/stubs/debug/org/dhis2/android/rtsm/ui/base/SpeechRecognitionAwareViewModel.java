package org.dhis2.android.rtsm.ui.base;

import org.dhis2.android.rtsm.data.SpeechRecognitionState;
import org.dhis2.android.rtsm.services.SpeechRecognitionManager;
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\b\u0016\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bJ\u0006\u0010\n\u001a\u00020\u000bJ\u0006\u0010\f\u001a\u00020\u000bJ\u0006\u0010\r\u001a\u00020\u000bJ\u0006\u0010\u000e\u001a\u00020\u000bR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lorg/dhis2/android/rtsm/ui/base/SpeechRecognitionAwareViewModel;", "Lorg/dhis2/android/rtsm/ui/base/BaseViewModel;", "schedulerProvider", "Lorg/dhis2/android/rtsm/services/scheduler/BaseSchedulerProvider;", "speechRecognitionManager", "Lorg/dhis2/android/rtsm/services/SpeechRecognitionManager;", "(Lorg/dhis2/android/rtsm/services/scheduler/BaseSchedulerProvider;Lorg/dhis2/android/rtsm/services/SpeechRecognitionManager;)V", "getSpeechStatus", "Landroidx/lifecycle/LiveData;", "Lorg/dhis2/android/rtsm/data/SpeechRecognitionState;", "resetSpeechStatus", "", "startListening", "stopListening", "toggleSpeechRecognitionState", "psm-v2.9-DEV_debug"})
public class SpeechRecognitionAwareViewModel extends org.dhis2.android.rtsm.ui.base.BaseViewModel {
    @org.jetbrains.annotations.NotNull
    private final org.dhis2.android.rtsm.services.SpeechRecognitionManager speechRecognitionManager = null;
    
    @javax.inject.Inject
    public SpeechRecognitionAwareViewModel(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider schedulerProvider, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.services.SpeechRecognitionManager speechRecognitionManager) {
        super(null);
    }
    
    public final void startListening() {
    }
    
    public final void stopListening() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.LiveData<org.dhis2.android.rtsm.data.SpeechRecognitionState> getSpeechStatus() {
        return null;
    }
    
    public final void toggleSpeechRecognitionState() {
    }
    
    public final void resetSpeechStatus() {
    }
}