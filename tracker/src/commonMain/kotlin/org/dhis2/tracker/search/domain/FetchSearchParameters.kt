package org.dhis2.tracker.search.domain

import kotlinx.coroutines.withContext
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.tracker.input.model.TrackerInputType
import org.dhis2.tracker.search.data.SearchParametersRepository
import org.dhis2.tracker.search.model.FetchSearchParametersData
import org.dhis2.tracker.search.model.SearchParameterModel

class FetchSearchParameters(
    val dispatcher: Dispatcher,
    val repository: SearchParametersRepository,
) : UseCase<FetchSearchParametersData, List<SearchParameterModel>> {
    val hiddenInputTypes =
        listOf(
            TrackerInputType.IMAGE,
            TrackerInputType.COORDINATES,
            TrackerInputType.LETTER,
            TrackerInputType.UNIT_INTERVAL,
            TrackerInputType.COORDINATES,
            TrackerInputType.URL,
            TrackerInputType.NOT_SUPPORTED,
        )

    override suspend fun invoke(input: FetchSearchParametersData): Result<List<SearchParameterModel>> =
        withContext(dispatcher.io) {
            try {
                val searchParameters =
                    input.programUid?.let {
                        repository.getSearchParametersByProgram(it)
                    } ?: repository.getSearchParametersByTrackedEntityType(input.teiTypeUid)

                val filteredSearchParameters =
                    searchParameters
                        .filter {
                            it.inputType !in hiddenInputTypes
                        }

                Result.success(sortSearchParameters(filteredSearchParameters))
            } catch (e: DomainError) {
                Result.failure(e)
            }
        }

    // Sort parameters to list first QR or BarCode uniques, then QR or BarCode and then remaining uniques
    internal fun sortSearchParameters(parameters: List<SearchParameterModel>): List<SearchParameterModel> =
        parameters.sortedWith(
            compareByDescending<SearchParameterModel> {
                isQrCodeOrBarCode(it.inputType) && it.isUnique
            }.thenByDescending {
                isQrCodeOrBarCode(it.inputType)
            }.thenByDescending { it.isUnique },
        )

    internal fun isQrCodeOrBarCode(inputType: TrackerInputType?): Boolean =
        inputType == TrackerInputType.QR_CODE || inputType == TrackerInputType.BAR_CODE
}
