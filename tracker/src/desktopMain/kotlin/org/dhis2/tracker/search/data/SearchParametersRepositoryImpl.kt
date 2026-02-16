package org.dhis2.tracker.search.data

import org.dhis2.tracker.search.model.SearchParameterModel

class SearchParametersRepositoryImpl : SearchParametersRepository {
    override suspend fun getSearchParametersByProgram(programUid: String): List<SearchParameterModel> {
        TODO("Not yet implemented")
    }

    override suspend fun getSearchParametersByTrackedEntityType(trackedEntityTypeUid: String): List<SearchParameterModel> {
        TODO("Not yet implemented")
    }
}
