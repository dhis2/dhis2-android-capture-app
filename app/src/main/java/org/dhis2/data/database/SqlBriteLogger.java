package org.dhis2.data.database;


import com.squareup.sqlbrite2.SqlBrite;

import timber.log.Timber;

class SqlBriteLogger implements SqlBrite.Logger {
    private static final String TAG = SqlBriteLogger.class.getSimpleName();

    @Override
    public void log(String message) {
        Timber.tag(TAG);
        Timber.d(message);
    }
}
