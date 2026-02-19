package org.dhis2.tracker.search.data

import org.dhis2.tracker.search.model.SearchParameterModel

interface SearchParametersRepository {
    suspend fun getSearchParametersByProgram(programUid: String): List<SearchParameterModel>

    suspend fun getSearchParametersByTrackedEntityType(trackedEntityTypeUid: String): List<SearchParameterModel>
}
