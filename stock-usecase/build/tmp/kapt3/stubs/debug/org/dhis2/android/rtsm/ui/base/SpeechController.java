package org.dhis2.android.rtsm.ui.base;

import org.dhis2.android.rtsm.data.SpeechRecognitionState;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\bf\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&J+\u0010\u0006\u001a\u00020\u00032!\u0010\u0007\u001a\u001d\u0012\u0013\u0012\u00110\u0005\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u0004\u0012\u0004\u0012\u00020\u00030\bH&J\b\u0010\u000b\u001a\u00020\u0003H&J+\u0010\f\u001a\u00020\u00032!\u0010\u0007\u001a\u001d\u0012\u0013\u0012\u00110\u0005\u00a2\u0006\f\b\t\u0012\b\b\n\u0012\u0004\b\b(\u0004\u0012\u0004\u0012\u00020\u00030\bH&\u00a8\u0006\r"}, d2 = {"Lorg/dhis2/android/rtsm/ui/base/SpeechController;", "", "onStateChange", "", "state", "Lorg/dhis2/android/rtsm/data/SpeechRecognitionState;", "startListening", "callback", "Lkotlin/Function1;", "Lkotlin/ParameterName;", "name", "stopListening", "toggleState", "psm-v2.9-DEV_debug"})
public abstract interface SpeechController {
    
    public abstract void startListening(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super org.dhis2.android.rtsm.data.SpeechRecognitionState, kotlin.Unit> callback);
    
    public abstract void stopListening();
    
    public abstract void onStateChange(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.SpeechRecognitionState state);
    
    public abstract void toggleState(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super org.dhis2.android.rtsm.data.SpeechRecognitionState, kotlin.Unit> callback);
}