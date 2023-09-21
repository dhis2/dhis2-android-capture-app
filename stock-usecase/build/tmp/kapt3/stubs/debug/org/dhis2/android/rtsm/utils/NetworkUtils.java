package org.dhis2.android.rtsm.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import timber.log.Timber;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\u0018\u0000 \u00032\u00020\u0001:\u0001\u0003B\u0005\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0004"}, d2 = {"Lorg/dhis2/android/rtsm/utils/NetworkUtils;", "", "()V", "Companion", "psm-v2.9-DEV_debug"})
public final class NetworkUtils {
    @org.jetbrains.annotations.NotNull
    public static final org.dhis2.android.rtsm.utils.NetworkUtils.Companion Companion = null;
    
    public NetworkUtils() {
        super();
    }
    
    @kotlin.jvm.JvmStatic
    public static final boolean isOnline(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        return false;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007\u00a8\u0006\u0007"}, d2 = {"Lorg/dhis2/android/rtsm/utils/NetworkUtils$Companion;", "", "()V", "isOnline", "", "context", "Landroid/content/Context;", "psm-v2.9-DEV_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @kotlin.jvm.JvmStatic
        public final boolean isOnline(@org.jetbrains.annotations.NotNull
        android.content.Context context) {
            return false;
        }
    }
}