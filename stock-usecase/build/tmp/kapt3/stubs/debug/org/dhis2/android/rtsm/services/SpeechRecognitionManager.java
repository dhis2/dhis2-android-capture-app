package org.dhis2.android.rtsm.services;

import androidx.lifecycle.LiveData;
import org.dhis2.android.rtsm.data.SpeechRecognitionState;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\bf\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&J\u000e\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H&J\b\u0010\u0007\u001a\u00020\u0003H&J\b\u0010\b\u001a\u00020\u0003H&J\b\u0010\t\u001a\u00020\u0003H&J\b\u0010\n\u001a\u00020\u0003H&J\u0010\u0010\u000b\u001a\u00020\u00032\u0006\u0010\f\u001a\u00020\rH&\u00a8\u0006\u000e"}, d2 = {"Lorg/dhis2/android/rtsm/services/SpeechRecognitionManager;", "", "cleanUp", "", "getStatus", "Landroidx/lifecycle/LiveData;", "Lorg/dhis2/android/rtsm/data/SpeechRecognitionState;", "resetStatus", "restart", "start", "stop", "supportNegativeNumberInput", "allow", "", "psm-v2.9-DEV_debug"})
public abstract interface SpeechRecognitionManager {
    
    public abstract void start();
    
    public abstract void restart();
    
    public abstract void stop();
    
    public abstract void cleanUp();
    
    public abstract void resetStatus();
    
    @org.jetbrains.annotations.NotNull
    public abstract androidx.lifecycle.LiveData<org.dhis2.android.rtsm.data.SpeechRecognitionState> getStatus();
    
    public abstract void supportNegativeNumberInput(boolean allow);
}