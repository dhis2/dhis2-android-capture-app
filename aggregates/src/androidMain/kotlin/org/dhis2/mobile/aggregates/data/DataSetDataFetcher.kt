package org.dhis2.mobile.aggregates.data

import org.hisp.dhis.android.core.D2

class DataSetDataFetcher(
    private val d2: D2,
) : DataFetcher {
    override fun test(): String = d2.userModule().user().blockingGet()?.let {
        "{username: ${it.username()}}"
    } ?: "No user"
}
