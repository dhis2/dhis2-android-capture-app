package org.dhis2.tracker.search.data

interface FilterRepository {
    suspend fun getFiltered
}