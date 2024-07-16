package org.dhis2.usescases.searchTrackEntity

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.dhis2.data.search.SearchParametersModel
import org.dhis2.form.model.FieldUiModel
import org.dhis2.maps.model.MapItemModel
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchItem

interface SearchRepositoryKt {

    fun searchTrackedEntities(
        searchParametersModel: SearchParametersModel,
        isOnline: Boolean,
    ): Flow<PagingData<TrackedEntitySearchItem>>

    suspend fun searchParameters(programUid: String?, teiTypeUid: String): List<FieldUiModel>

    suspend fun searchTrackedEntitiesImmediate(
        searchParametersModel: SearchParametersModel,
        isOnline: Boolean,
    ): List<TrackedEntitySearchItem>

    fun searchTeiForMap(
        searchParametersModel: SearchParametersModel,
        isOnline: Boolean,
    ): List<MapItemModel>

    fun searchEventForMap(
        teiUids: List<String>,
        selectedProgram: Program?,
    ): List<MapItemModel>

    fun searchRelationshipsForMap(
        teis: List<MapItemModel>,
        selectedProgram: Program?,
    ): List<MapItemModel>
}
