package org.dhis2.usescases.searchTrackEntity

import org.dhis2.commons.filters.sorting.SortingItem
import org.dhis2.data.search.SearchParametersModel
import org.dhis2.maps.model.MapItemModel
import org.dhis2.mobile.commons.model.CustomIntentModel
import org.dhis2.tracker.input.model.TrackerInputType
import org.dhis2.tracker.input.ui.action.FieldUid
import org.dhis2.tracker.search.model.TrackedEntitySearchItemResult
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.program.Program

interface SearchRepositoryKt {
    fun searchTeiForMap(
        searchParametersModel: SearchParametersModel,
        isOnline: Boolean,
    ): List<MapItemModel>

    fun validateValue(
        inputType: TrackerInputType,
        value: String,
    ): Any

    fun searchEventForMap(
        teiUids: List<String>,
        selectedProgram: Program?,
    ): List<MapItemModel>

    fun searchRelationshipsForMap(
        teis: List<MapItemModel>,
        selectedProgram: Program?,
    ): List<MapItemModel>

    suspend fun getCustomIntent(fieldUid: FieldUid): CustomIntentModel?

    fun saveSearchValuesAndGetAllowCache(
        queryData: MutableMap<String, List<String>?>?,
        programUid: String?,
    ): Boolean

    fun getExcludeValues(): HashSet<String>?

    fun trackerValueTypeToSDKValueType(trackerInputType: TrackerInputType): ValueType?

    fun mapTrackedEntitySearchItemResultToSearchTeiModel(
        searchItemResult: TrackedEntitySearchItemResult,
        sortingItem: SortingItem?,
    ): SearchTeiModel
}
