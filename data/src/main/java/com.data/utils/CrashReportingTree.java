package com.data.utils;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

import timber.log.Timber;

public final class CrashReportingTree extends Timber.Tree {

    @Override
    protected void log(int priority, String tag, String message, Throwable throwable) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return;
        }

        Crashlytics.log(priority, tag, message);
        if (priority == Log.ERROR && throwable != null) {
            Crashlytics.logException(throwable);
        }
    }
}