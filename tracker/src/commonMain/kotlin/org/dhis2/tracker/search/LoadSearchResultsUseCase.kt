package org.dhis2.tracker.search

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.dhis2.mobile.commons.customintents.CustomIntentRepository
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.commons.model.CustomIntentActionTypeModel
import org.dhis2.tracker.search.data.LoadSearchResultsRepository
import org.dhis2.tracker.search.model.SearchTrackerParameterResult
import org.dhis2.tracker.search.model.SearchTrackerParametersModel

class LoadSearchResultsUseCase(
    private val repository: LoadSearchResultsRepository,
    private val customIntentRepository: CustomIntentRepository,
    private val teType: String,
) : UseCase<SearchTrackerParametersModel, Flow<PagingData<SearchTrackerParameterResult>>> {
    override suspend fun invoke(input: SearchTrackerParametersModel): Result<Flow<PagingData<SearchTrackerParameterResult>>> {

        try {
            prepareQuery(input, teType)
            if(input.excludeValues?.isNotEmpty() == true && input.selectedProgram == null){
                repository.excludeValuesFromQuery(input.excludeValues.toList())
            }

           return  Result.success(repository.fetchResults(
               isOnline = input.isOnline,
               hasStateFilters = input.hasStateFilters,
               allowCache = input.allowCache,
           ))
        } catch (domainError: DomainError) {

            //what kind of error object are we going to return?
            return Result.failure(domainError)
        }

    }

    private suspend fun prepareQuery(input: SearchTrackerParametersModel, teType: String) {
        repository.addFiltersToQuery(input.selectedProgram, teType)
        input.queryData?.let {
            for (i in it.keys.indices) {
                val dataId = input.queryData.keys.toTypedArray()[i]
                var dataValues: MutableList<String>? = input.queryData[dataId]?.toMutableList()
                val isTETypeAttribute = repository.isTETypeAttribute(teType, dataId)


                if (input.selectedProgram != null || isTETypeAttribute) {
                    val teAttribute = repository.getTEAttribute(dataId)
                    dataValues?.let {
                        if (!customIntentRepository.attributeHasCustomIntentAndReturnsAListOfValues(
                                dataId,
                                CustomIntentActionTypeModel.SEARCH
                            ) && dataValues.size > 1
                        ) {
                            dataValues = mutableListOf(dataValues.joinToString(","))
                        }
                        repository.addToQuery(
                            dataId,
                            dataValues,
                            teAttribute.isUnique,
                            teAttribute.isOptionSet
                        )
                    }
                }
            }
        }
    }
}