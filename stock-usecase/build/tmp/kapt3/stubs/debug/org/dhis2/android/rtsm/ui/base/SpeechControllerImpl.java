package org.dhis2.android.rtsm.ui.base;

import org.dhis2.android.rtsm.data.SpeechRecognitionState;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\t\u001a\u00020\b2\u0006\u0010\n\u001a\u00020\u0007H\u0016J+\u0010\u000b\u001a\u00020\b2!\u0010\u0005\u001a\u001d\u0012\u0013\u0012\u00110\u0007\u00a2\u0006\f\b\f\u0012\b\b\r\u0012\u0004\b\b(\n\u0012\u0004\u0012\u00020\b0\u0006H\u0016J\b\u0010\u000e\u001a\u00020\bH\u0016J+\u0010\u000f\u001a\u00020\b2!\u0010\u0005\u001a\u001d\u0012\u0013\u0012\u00110\u0007\u00a2\u0006\f\b\f\u0012\b\b\r\u0012\u0004\b\b(\n\u0012\u0004\u0012\u00020\b0\u0006H\u0016R\u001c\u0010\u0005\u001a\u0010\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\b\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0010"}, d2 = {"Lorg/dhis2/android/rtsm/ui/base/SpeechControllerImpl;", "Lorg/dhis2/android/rtsm/ui/base/SpeechController;", "viewModel", "Lorg/dhis2/android/rtsm/ui/base/SpeechRecognitionAwareViewModel;", "(Lorg/dhis2/android/rtsm/ui/base/SpeechRecognitionAwareViewModel;)V", "callback", "Lkotlin/Function1;", "Lorg/dhis2/android/rtsm/data/SpeechRecognitionState;", "", "onStateChange", "state", "startListening", "Lkotlin/ParameterName;", "name", "stopListening", "toggleState", "psm-v2.9-DEV_debug"})
public final class SpeechControllerImpl implements org.dhis2.android.rtsm.ui.base.SpeechController {
    @org.jetbrains.annotations.NotNull
    private final org.dhis2.android.rtsm.ui.base.SpeechRecognitionAwareViewModel viewModel = null;
    @org.jetbrains.annotations.Nullable
    private kotlin.jvm.functions.Function1<? super org.dhis2.android.rtsm.data.SpeechRecognitionState, kotlin.Unit> callback;
    
    public SpeechControllerImpl(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.ui.base.SpeechRecognitionAwareViewModel viewModel) {
        super();
    }
    
    @java.lang.Override
    public void onStateChange(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.SpeechRecognitionState state) {
    }
    
    @java.lang.Override
    public void startListening(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super org.dhis2.android.rtsm.data.SpeechRecognitionState, kotlin.Unit> callback) {
    }
    
    @java.lang.Override
    public void stopListening() {
    }
    
    @java.lang.Override
    public void toggleState(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super org.dhis2.android.rtsm.data.SpeechRecognitionState, kotlin.Unit> callback) {
    }
}