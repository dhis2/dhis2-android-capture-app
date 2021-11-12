package org.dhis2.usescases.searchTrackEntity

import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel
import org.hisp.dhis.android.core.program.Program

class SearchMessageMapper(private val searchResources: SearchResources) {

    fun getSearchMessage(
        list: List<SearchTeiModel>,
        program: Program?,
        searchParameters: Map<String, String>,
        typeHasAttributes: Boolean,
        defaultMaxTei: Int = 5,
        teTypeName: String
    ): SearchMessageResult = when {
        listIsOnlineError(list) -> listOnlineMessageResult(list)
        program != null -> programMessageResult(list, program, searchParameters, teTypeName)
        else -> noProgramMessageResult(
            list,
            searchParameters,
            typeHasAttributes,
            defaultMaxTei,
            teTypeName
        )
    }

    private fun listIsOnlineError(list: List<SearchTeiModel>): Boolean {
        return list.isNotEmpty() && list.all { searchTeiModel ->
            searchTeiModel.onlineErrorMessage != null
        }
    }

    private fun listOnlineMessageResult(list: List<SearchTeiModel>): SearchMessageResult {
        return SearchMessageResult(
            message = list.joinToString("\n") { it.onlineErrorMessage.toString() },
            forceSearch = false
        )
    }

    private fun programMessageResult(
        list: List<SearchTeiModel>,
        program: Program,
        searchParameters: Map<String, String>,
        teTypeName: String
    ): SearchMessageResult {
        return if (isSearching(searchParameters)) {
            when {
                minNumberOfAttributesCheckFailed(program, searchParameters) ->
                    minNumberOfAttributesResult(program.minAttributesRequiredToSearch() ?: 0)
                maxNumberOfTeisToReturnCheckFailed(list, program) ->
                    maxTeiReachedResult(program.maxTeiCountToReturn() ?: 0)
                searchReturnedEmptyList(list) ->
                    searchDidNotReturnedResult(teTypeName)
                else ->
                    searchSuccessfulResult()
            }
        } else {
            when {
                programCanDisplayFrontPage(program, searchParameters) ->
                    displayFrontPageResult()
                else ->
                    initSearchResult()
            }
        }
    }

    private fun noProgramMessageResult(
        list: List<SearchTeiModel>,
        searchParameters: Map<String, String>,
        teTypeHasPublicAttributes: Boolean,
        maxNumberToReturnForTeType: Int,
        teTypeName: String
    ): SearchMessageResult {
        return if (!teTypeHasPublicAttributes) {
            noAttributesToSearchResult(teTypeName)
        } else if (isSearching(searchParameters)) {
            when {
                searchReturnedEmptyList(list) ->
                    searchDidNotReturnedResult(teTypeName)
                maxNumberOfTeisToReturnCheckFailed(list, maxNumberToReturnForTeType) ->
                    maxTeiReachedResult(maxNumberToReturnForTeType)
                else ->
                    searchSuccessfulResult()
            }
        } else {
            initSearchResult()
        }
    }

    private fun isSearching(searchParameters: Map<String, String>): Boolean {
        return searchParameters.isNotEmpty()
    }

    private fun minNumberOfAttributesCheckFailed(
        program: Program,
        searchParameters: Map<String, String>
    ): Boolean {
        return program.minAttributesRequiredToSearch() ?: 0 > 0 &&
            searchParameters.size < program.minAttributesRequiredToSearch() ?: 0
    }

    private fun maxNumberOfTeisToReturnCheckFailed(
        list: List<SearchTeiModel>,
        program: Program
    ): Boolean {
        return program.maxTeiCountToReturn() ?: 0 > 0 &&
            list.filter { it.onlineErrorMessage == null }.size >= program.maxTeiCountToReturn() ?: 0
    }

    private fun maxNumberOfTeisToReturnCheckFailed(
        list: List<SearchTeiModel>,
        maxNumberToReturnForTeType: Int
    ): Boolean {
        return maxNumberToReturnForTeType > 0 &&
            list.filter { it.onlineErrorMessage == null }.size >= maxNumberToReturnForTeType
    }

    private fun searchReturnedEmptyList(
        list: List<SearchTeiModel>
    ): Boolean {
        return list.none { it.onlineErrorMessage == null }
    }

    private fun programCanDisplayFrontPage(
        program: Program,
        searchParameters: Map<String, String>
    ): Boolean {
        return program.displayFrontPageList() == true && searchParameters.isEmpty()
    }

    private fun minNumberOfAttributesResult(minNumber: Int) = SearchMessageResult(
        message = searchResources.searchMinNumAttributes(minNumber),
        canRegister = false,
        showButton = false,
        forceSearch = false
    )

    private fun maxTeiReachedResult(maxNumber: Int) = SearchMessageResult(
        message = searchResources.searchMaxTeiReached(maxNumber),
        canRegister = false,
        showButton = false,
        forceSearch = false
    )

    private fun searchDidNotReturnedResult(typeName: String) = SearchMessageResult(
        message = searchResources.searchCriteriaNotMet(typeName),
        canRegister = true,
        showButton = false,
        forceSearch = false
    )

    private fun searchSuccessfulResult() = SearchMessageResult(
        canRegister = true,
        showButton = true,
        forceSearch = false
    )

    private fun displayFrontPageResult() = SearchMessageResult(
        forceSearch = false
    )

    private fun initSearchResult() = SearchMessageResult(
        message = searchResources.searchInit(),
        canRegister = false,
        showButton = false,
        forceSearch = true
    )

    private fun noAttributesToSearchResult(typeName: String) = SearchMessageResult(
        message = searchResources.teiTypeHasNoAttributes(typeName),
        canRegister = false,
        showButton = false,
        forceSearch = false
    )
}
