package org.dhis2.tracker.search

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.dhis2.mobile.commons.customintents.CustomIntentRepository
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.commons.model.CustomIntentActionTypeModel
import org.dhis2.tracker.search.data.FilterRepository
import org.dhis2.tracker.search.data.LoadSearchResultsRepository
import org.dhis2.tracker.search.model.SearchTrackerParameterResult
import org.dhis2.tracker.search.model.SearchTrackerParametersModel

class LoadSearchResultsUseCase(
    private val repository: LoadSearchResultsRepository,
    private val customIntentRepository: CustomIntentRepository,
    //filter repositor might not be needed
    private val filterRepository: FilterRepository,
    private val teType: String,
) : UseCase<SearchTrackerParametersModel, Flow<PagingData<SearchTrackerParameterResult>>> {
    override suspend fun invoke(input: SearchTrackerParametersModel): Result<Flow<PagingData<SearchTrackerParameterResult>>> {

        try {
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

            //TODO Implement logic to fetch final results in method from searchTrackedEntities() and trackedEntitySearchQuery() ( SearchRepositoryImplkt)
            Result.success()
        } catch (domainError: DomainError) {
            Result.failure(domainError)
        }

    }

}