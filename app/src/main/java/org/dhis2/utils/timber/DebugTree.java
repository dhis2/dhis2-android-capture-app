package org.dhis2.utils.timber;

import android.util.Log;

import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 06/06/2018.
 */

public class DebugTree extends Timber.Tree {
    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        Log.d(tag, message);
    }
}
