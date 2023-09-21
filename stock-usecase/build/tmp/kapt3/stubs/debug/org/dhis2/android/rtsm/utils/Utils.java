package org.dhis2.android.rtsm.utils;

import android.text.TextUtils;
import timber.log.Timber;
import java.util.regex.Pattern;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\u0018\u0000 \u00032\u00020\u0001:\u0001\u0003B\u0005\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0004"}, d2 = {"Lorg/dhis2/android/rtsm/utils/Utils;", "", "()V", "Companion", "psm-v2.9-DEV_debug"})
public final class Utils {
    @org.jetbrains.annotations.NotNull
    public static final org.dhis2.android.rtsm.utils.Utils.Companion Companion = null;
    
    public Utils() {
        super();
    }
    
    @kotlin.jvm.JvmStatic
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String capitalizeText(@org.jetbrains.annotations.NotNull
    java.lang.String text) {
        return null;
    }
    
    @kotlin.jvm.JvmStatic
    public static final boolean isSignedNumeric(@org.jetbrains.annotations.NotNull
    java.lang.String s) {
        return false;
    }
    
    @kotlin.jvm.JvmStatic
    public static final boolean isValidStockOnHand(@org.jetbrains.annotations.Nullable
    java.lang.String value) {
        return false;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0004H\u0007J\u000e\u0010\u0006\u001a\u00020\u00042\u0006\u0010\u0007\u001a\u00020\u0004J\u0010\u0010\b\u001a\u00020\t2\u0006\u0010\u0007\u001a\u00020\u0004H\u0007J\u0012\u0010\n\u001a\u00020\t2\b\u0010\u000b\u001a\u0004\u0018\u00010\u0004H\u0007\u00a8\u0006\f"}, d2 = {"Lorg/dhis2/android/rtsm/utils/Utils$Companion;", "", "()V", "capitalizeText", "", "text", "cleanUpSignedNumber", "s", "isSignedNumeric", "", "isValidStockOnHand", "value", "psm-v2.9-DEV_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @kotlin.jvm.JvmStatic
        public final boolean isValidStockOnHand(@org.jetbrains.annotations.Nullable
        java.lang.String value) {
            return false;
        }
        
        /**
         * Checks if a string is a signed numeric
         *
         * @param s The string to check
         * @return A Boolean denoting the outcome of the operation
         */
        @kotlin.jvm.JvmStatic
        public final boolean isSignedNumeric(@org.jetbrains.annotations.NotNull
        java.lang.String s) {
            return false;
        }
        
        /**
         * Removes extraneous space from signed numbers
         *
         * For example,
         *
         * cleanUpSignedNumber("- 12") = -12
         *
         * @return The clean number string
         */
        @org.jetbrains.annotations.NotNull
        public final java.lang.String cleanUpSignedNumber(@org.jetbrains.annotations.NotNull
        java.lang.String s) {
            return null;
        }
        
        @kotlin.jvm.JvmStatic
        @org.jetbrains.annotations.NotNull
        public final java.lang.String capitalizeText(@org.jetbrains.annotations.NotNull
        java.lang.String text) {
            return null;
        }
    }
}