package org.dhis2.utils.timber;

import androidx.annotation.NonNull;

import android.util.Log;

import org.dhis2.commons.reporting.CrashReportController;
import org.jetbrains.annotations.Nullable;

import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 06/06/2018.
 */

public class ReleaseTree extends Timber.Tree {

    public CrashReportController crashReportController;

    public ReleaseTree(CrashReportController crashReportController) {
        this.crashReportController = crashReportController;
    }

    @Override
    protected boolean isLoggable(@Nullable String tag, int priority) {
        // Don't log VERBOSE, DEBUG and INFO only ERROR, WARN and WTF
        return priority != Log.VERBOSE && priority != Log.DEBUG && priority != Log.INFO;
    }

    @Override
    protected void log(int priority, String tag, @NonNull final String message, final Throwable t) {
        if (isLoggable(tag, priority)) {
            Exception e = new Exception(t);
            crashReportController.trackError(e, e.getMessage());
        }
    }
}
