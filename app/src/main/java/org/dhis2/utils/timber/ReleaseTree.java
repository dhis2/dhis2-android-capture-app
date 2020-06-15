package org.dhis2.utils.timber;

import androidx.annotation.NonNull;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.jetbrains.annotations.Nullable;

import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 06/06/2018.
 */

public class ReleaseTree extends Timber.Tree {

    @Override
    protected boolean isLoggable(@Nullable String tag, int priority) {
        // Don't log VERBOSE, DEBUG and INFO only ERROR, WARN and WTF
        return priority != Log.VERBOSE && priority != Log.DEBUG && priority != Log.INFO;
    }

    @Override
    protected void log(int priority, String tag, @NonNull final String message, final Throwable t) {
        if (isLoggable(tag, priority))
            Crashlytics.log(priority, tag, message);
    }
}
