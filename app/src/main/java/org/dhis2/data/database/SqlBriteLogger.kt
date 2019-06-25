package org.dhis2.data.database


import com.squareup.sqlbrite2.SqlBrite

import timber.log.Timber

internal class SqlBriteLogger : SqlBrite.Logger {

    override fun log(message: String) {
        Timber.tag(TAG)
        Timber.d(message)
    }

    companion object {
        private val TAG = SqlBriteLogger::class.java.simpleName
    }
}
