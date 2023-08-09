package org.dhis2.usescases.uiboost.data.repository

import javax.inject.Inject
import org.hisp.dhis.android.core.D2
import timber.log.Timber

class UBDataStoreRepositoryImpl @Inject constructor(
    private val d2: D2
) : UBDataStoreRepository {
    override suspend fun downloadDataStore() {
        d2.dataStoreModule().dataStoreDownloader().download()
        val result = d2.dataStoreModule().dataStore().blockingGet()
    }
}
