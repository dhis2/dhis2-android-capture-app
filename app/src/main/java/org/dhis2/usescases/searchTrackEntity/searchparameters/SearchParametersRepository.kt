package org.dhis2.usescases.searchTrackEntity.searchparameters

import org.dhis2.usescases.searchTrackEntity.searchparameters.model.SearchParameter

class SearchParametersRepository {

    fun searchParameters(): List<SearchParameter> {
        return listOf(SearchParameter("uid"))
    }
}
