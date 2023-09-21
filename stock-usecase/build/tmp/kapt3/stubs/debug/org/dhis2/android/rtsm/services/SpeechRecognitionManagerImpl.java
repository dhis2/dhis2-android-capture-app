package org.dhis2.android.rtsm.services;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.TextUtils;
import androidx.lifecycle.MutableLiveData;
import org.dhis2.android.rtsm.commons.Constants;
import org.dhis2.android.rtsm.data.SpeechRecognitionState;
import org.dhis2.android.rtsm.utils.Utils;
import timber.log.Timber;
import java.util.Locale;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000b\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0012\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0007\n\u0002\b\t\n\u0002\u0010\u000e\n\u0000\u0018\u00002\u00020\u00012\u00020\u0002B\r\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\u0002\u0010\u0005J\b\u0010\u000e\u001a\u00020\u000fH\u0016J\b\u0010\u0010\u001a\u00020\u0011H\u0002J\u000e\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\b0\u0007H\u0016J\b\u0010\u0013\u001a\u00020\u000fH\u0002J\b\u0010\u0014\u001a\u00020\u000fH\u0016J\u0012\u0010\u0015\u001a\u00020\u000f2\b\u0010\u0016\u001a\u0004\u0018\u00010\u0017H\u0016J\b\u0010\u0018\u001a\u00020\u000fH\u0016J\u0010\u0010\u0019\u001a\u00020\u000f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0016J\u001a\u0010\u001c\u001a\u00020\u000f2\u0006\u0010\u001d\u001a\u00020\u001b2\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0016J\u0012\u0010 \u001a\u00020\u000f2\b\u0010!\u001a\u0004\u0018\u00010\u001fH\u0016J\u0012\u0010\"\u001a\u00020\u000f2\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0016J\u0012\u0010#\u001a\u00020\u000f2\b\u0010$\u001a\u0004\u0018\u00010\u001fH\u0016J\u0010\u0010%\u001a\u00020\u000f2\u0006\u0010&\u001a\u00020\'H\u0016J\b\u0010(\u001a\u00020\u000fH\u0016J\b\u0010)\u001a\u00020\u000fH\u0016J\b\u0010*\u001a\u00020\u000fH\u0002J\b\u0010+\u001a\u00020\u000fH\u0016J\b\u0010,\u001a\u00020\u000fH\u0016J\u0010\u0010-\u001a\u00020\u000f2\u0006\u0010.\u001a\u00020\nH\u0016J\u0010\u0010/\u001a\u00020\b2\u0006\u00100\u001a\u000201H\u0002R\u0014\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u00062"}, d2 = {"Lorg/dhis2/android/rtsm/services/SpeechRecognitionManagerImpl;", "Lorg/dhis2/android/rtsm/services/SpeechRecognitionManager;", "Landroid/speech/RecognitionListener;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "_speechRecognitionStatus", "Landroidx/lifecycle/MutableLiveData;", "Lorg/dhis2/android/rtsm/data/SpeechRecognitionState;", "allowNegativeNumberInput", "", "readyForSpeech", "speechRecognizer", "Landroid/speech/SpeechRecognizer;", "cleanUp", "", "getIntent", "Landroid/content/Intent;", "getStatus", "initialize", "onBeginningOfSpeech", "onBufferReceived", "buffer", "", "onEndOfSpeech", "onError", "errorCode", "", "onEvent", "eventType", "params", "Landroid/os/Bundle;", "onPartialResults", "partialResults", "onReadyForSpeech", "onResults", "bundle", "onRmsChanged", "rmsdB", "", "resetStatus", "restart", "setup", "start", "stop", "supportNegativeNumberInput", "allow", "validateInput", "inputStr", "", "psm-v2.9-DEV_debug"})
public final class SpeechRecognitionManagerImpl implements org.dhis2.android.rtsm.services.SpeechRecognitionManager, android.speech.RecognitionListener {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.Nullable
    private android.speech.SpeechRecognizer speechRecognizer;
    private boolean readyForSpeech = false;
    private boolean allowNegativeNumberInput = false;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<org.dhis2.android.rtsm.data.SpeechRecognitionState> _speechRecognitionStatus = null;
    
    public SpeechRecognitionManagerImpl(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        super();
    }
    
    private final void setup() {
    }
    
    private final void initialize() {
    }
    
    @java.lang.Override
    public void start() {
    }
    
    @java.lang.Override
    public void restart() {
    }
    
    @java.lang.Override
    public void stop() {
    }
    
    @java.lang.Override
    public void cleanUp() {
    }
    
    @java.lang.Override
    public void resetStatus() {
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public androidx.lifecycle.MutableLiveData<org.dhis2.android.rtsm.data.SpeechRecognitionState> getStatus() {
        return null;
    }
    
    @java.lang.Override
    public void supportNegativeNumberInput(boolean allow) {
    }
    
    private final android.content.Intent getIntent() {
        return null;
    }
    
    @java.lang.Override
    public void onReadyForSpeech(@org.jetbrains.annotations.Nullable
    android.os.Bundle params) {
    }
    
    @java.lang.Override
    public void onError(int errorCode) {
    }
    
    @java.lang.Override
    public void onResults(@org.jetbrains.annotations.Nullable
    android.os.Bundle bundle) {
    }
    
    private final org.dhis2.android.rtsm.data.SpeechRecognitionState validateInput(java.lang.String inputStr) {
        return null;
    }
    
    @java.lang.Override
    public void onBeginningOfSpeech() {
    }
    
    @java.lang.Override
    public void onEndOfSpeech() {
    }
    
    @java.lang.Override
    public void onPartialResults(@org.jetbrains.annotations.Nullable
    android.os.Bundle partialResults) {
    }
    
    @java.lang.Override
    public void onEvent(int eventType, @org.jetbrains.annotations.Nullable
    android.os.Bundle params) {
    }
    
    @java.lang.Override
    public void onRmsChanged(float rmsdB) {
    }
    
    @java.lang.Override
    public void onBufferReceived(@org.jetbrains.annotations.Nullable
    byte[] buffer) {
    }
}