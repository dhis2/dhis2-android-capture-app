package org.dhis2.tracker.search.domain

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.dhis2.mobile.commons.customintents.CustomIntentRepository
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.commons.model.CustomIntentActionTypeModel
import org.dhis2.tracker.search.data.SearchTrackedEntityRepository
import org.dhis2.tracker.search.model.SearchTrackedEntitiesInput
import org.dhis2.tracker.search.model.TrackedEntitySearchItemResult

class SearchTrackedEntities(
    private val repository: SearchTrackedEntityRepository,
    private val customIntentRepository: CustomIntentRepository,
    private val teType: String,
) : UseCase<SearchTrackedEntitiesInput, Flow<PagingData<TrackedEntitySearchItemResult>>> {
    override suspend fun invoke(input: SearchTrackedEntitiesInput): Result<Flow<PagingData<TrackedEntitySearchItemResult>>> {
        try {
            prepareQuery(input, teType)
            // Due to performance issues and possible duplicates we exclude the values in the exclude list only when no program is selected
            if (input.excludeValues?.isNotEmpty() == true && input.selectedProgram == null) {
                repository.excludeValuesFromQuery(input.excludeValues.toList())
            }
            // fetch results from repository based on the prepared query
            // and if the device is online and has state filters
            return Result.success(
                repository.fetchResults(
                    isOnline = input.isOnline,
                    hasStateFilters = input.hasStateFilters,
                    allowCache = input.allowCache,
                ),
            )
        } catch (domainError: DomainError) {
            // will manage error from view,
            return Result.failure(domainError)
        }
    }

    private suspend fun prepareQuery(
        input: SearchTrackedEntitiesInput,
        teType: String,
    ) {
        // Add filters from FilterManager
        repository.addFiltersToQuery(input.selectedProgram, teType)

        input.queryData?.let {
            // iterate through the query data and add to the repository query
            for (i in it.keys.indices) {
                val dataId = input.queryData.keys.toTypedArray()[i]
                var dataValues = input.queryData[dataId]?.toMutableList()

                // checks if the dataId is an attribute of the teType
                val isTETypeAttribute = repository.isTETypeAttribute(teType, dataId)

                if (input.selectedProgram != null || isTETypeAttribute) {
                    // fetches the teAttribute details (isUnique, isOptionSet)
                    val teAttribute = repository.getTEAttribute(dataId)
                    dataValues?.let {
                        // checks if the attribute has custom intent associated with it that returns multiple values
                        if (!customIntentRepository.attributeHasCustomIntentAndReturnsAListOfValues(
                                dataId,
                                CustomIntentActionTypeModel.SEARCH,
                            ) && dataValues.size > 1
                        ) {
                            // if it has multiple values, we join them into a single value separated by commas
                            dataValues = mutableListOf(dataValues.joinToString(","))
                        }

                        repository.addToQuery(
                            dataId,
                            dataValues,
                            teAttribute.isUnique,
                            teAttribute.isOptionSet,
                        )
                    }
                }
            }
        }
    }
}
