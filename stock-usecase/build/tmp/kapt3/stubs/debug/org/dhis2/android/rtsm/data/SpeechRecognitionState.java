package org.dhis2.android.rtsm.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0006\u0003\u0004\u0005\u0006\u0007\bB\u0007\b\u0004\u00a2\u0006\u0002\u0010\u0002\u0082\u0001\u0006\t\n\u000b\f\r\u000e\u00a8\u0006\u000f"}, d2 = {"Lorg/dhis2/android/rtsm/data/SpeechRecognitionState;", "", "()V", "Completed", "Errored", "NotAvailable", "NotInitialized", "Started", "Stopped", "Lorg/dhis2/android/rtsm/data/SpeechRecognitionState$Completed;", "Lorg/dhis2/android/rtsm/data/SpeechRecognitionState$Errored;", "Lorg/dhis2/android/rtsm/data/SpeechRecognitionState$NotAvailable;", "Lorg/dhis2/android/rtsm/data/SpeechRecognitionState$NotInitialized;", "Lorg/dhis2/android/rtsm/data/SpeechRecognitionState$Started;", "Lorg/dhis2/android/rtsm/data/SpeechRecognitionState$Stopped;", "psm-v2.9-DEV_debug"})
public abstract class SpeechRecognitionState {
    
    private SpeechRecognitionState() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u000f\u0012\b\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\u0002\u0010\u0004J\u000b\u0010\u0007\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0015\u0010\b\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0003H\u00d6\u0001R\u0013\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0010"}, d2 = {"Lorg/dhis2/android/rtsm/data/SpeechRecognitionState$Completed;", "Lorg/dhis2/android/rtsm/data/SpeechRecognitionState;", "data", "", "(Ljava/lang/String;)V", "getData", "()Ljava/lang/String;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "psm-v2.9-DEV_debug"})
    public static final class Completed extends org.dhis2.android.rtsm.data.SpeechRecognitionState {
        @org.jetbrains.annotations.Nullable
        private final java.lang.String data = null;
        
        public Completed(@org.jetbrains.annotations.Nullable
        java.lang.String data) {
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getData() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final org.dhis2.android.rtsm.data.SpeechRecognitionState.Completed copy(@org.jetbrains.annotations.Nullable
        java.lang.String data) {
            return null;
        }
        
        @java.lang.Override
        public boolean equals(@org.jetbrains.annotations.Nullable
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override
        @org.jetbrains.annotations.NotNull
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\t\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0003\b\u0086\b\u0018\u00002\u00020\u0001B\u0019\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006J\t\u0010\u000b\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\f\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u001f\u0010\r\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005H\u00c6\u0001J\u0013\u0010\u000e\u001a\u00020\u000f2\b\u0010\u0010\u001a\u0004\u0018\u00010\u0011H\u00d6\u0003J\t\u0010\u0012\u001a\u00020\u0003H\u00d6\u0001J\t\u0010\u0013\u001a\u00020\u0005H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\n\u00a8\u0006\u0014"}, d2 = {"Lorg/dhis2/android/rtsm/data/SpeechRecognitionState$Errored;", "Lorg/dhis2/android/rtsm/data/SpeechRecognitionState;", "code", "", "data", "", "(ILjava/lang/String;)V", "getCode", "()I", "getData", "()Ljava/lang/String;", "component1", "component2", "copy", "equals", "", "other", "", "hashCode", "toString", "psm-v2.9-DEV_debug"})
    public static final class Errored extends org.dhis2.android.rtsm.data.SpeechRecognitionState {
        private final int code = 0;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String data = null;
        
        public Errored(int code, @org.jetbrains.annotations.Nullable
        java.lang.String data) {
        }
        
        public final int getCode() {
            return 0;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getData() {
            return null;
        }
        
        public final int component1() {
            return 0;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final org.dhis2.android.rtsm.data.SpeechRecognitionState.Errored copy(int code, @org.jetbrains.annotations.Nullable
        java.lang.String data) {
            return null;
        }
        
        @java.lang.Override
        public boolean equals(@org.jetbrains.annotations.Nullable
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override
        @org.jetbrains.annotations.NotNull
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lorg/dhis2/android/rtsm/data/SpeechRecognitionState$NotAvailable;", "Lorg/dhis2/android/rtsm/data/SpeechRecognitionState;", "()V", "psm-v2.9-DEV_debug"})
    public static final class NotAvailable extends org.dhis2.android.rtsm.data.SpeechRecognitionState {
        @org.jetbrains.annotations.NotNull
        public static final org.dhis2.android.rtsm.data.SpeechRecognitionState.NotAvailable INSTANCE = null;
        
        private NotAvailable() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lorg/dhis2/android/rtsm/data/SpeechRecognitionState$NotInitialized;", "Lorg/dhis2/android/rtsm/data/SpeechRecognitionState;", "()V", "psm-v2.9-DEV_debug"})
    public static final class NotInitialized extends org.dhis2.android.rtsm.data.SpeechRecognitionState {
        @org.jetbrains.annotations.NotNull
        public static final org.dhis2.android.rtsm.data.SpeechRecognitionState.NotInitialized INSTANCE = null;
        
        private NotInitialized() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lorg/dhis2/android/rtsm/data/SpeechRecognitionState$Started;", "Lorg/dhis2/android/rtsm/data/SpeechRecognitionState;", "()V", "psm-v2.9-DEV_debug"})
    public static final class Started extends org.dhis2.android.rtsm.data.SpeechRecognitionState {
        @org.jetbrains.annotations.NotNull
        public static final org.dhis2.android.rtsm.data.SpeechRecognitionState.Started INSTANCE = null;
        
        private Started() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lorg/dhis2/android/rtsm/data/SpeechRecognitionState$Stopped;", "Lorg/dhis2/android/rtsm/data/SpeechRecognitionState;", "()V", "psm-v2.9-DEV_debug"})
    public static final class Stopped extends org.dhis2.android.rtsm.data.SpeechRecognitionState {
        @org.jetbrains.annotations.NotNull
        public static final org.dhis2.android.rtsm.data.SpeechRecognitionState.Stopped INSTANCE = null;
        
        private Stopped() {
        }
    }
}