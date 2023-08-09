package org.dhis2.usescases.uiboost.data.repository

interface UBDataStoreRepository {

    suspend fun downloadDataStore()
}
