package org.dhis2.utils.timber;

import android.os.Build;
import androidx.annotation.NonNull;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 06/06/2018.
 */

public class DebugTree extends Timber.DebugTree {
    @Override
    protected void log(int priority, String tag, @NonNull String message, Throwable t) {
        // Workaround for devices that doesn't show lower priority logs
        if (Build.MANUFACTURER.equals("HUAWEI") || Build.MANUFACTURER.equals("samsung")) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO)
                priority = Log.ERROR;
        }
        super.log(priority, tag, message, t);
    }

    @Override
    protected @Nullable String createStackElementTag(@NotNull StackTraceElement element) {
        // Add log statements line number to the log
        return super.createStackElementTag(element) + " - " + element.getLineNumber();
    }
}
