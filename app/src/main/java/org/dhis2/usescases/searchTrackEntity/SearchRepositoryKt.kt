package org.dhis2.usescases.searchTrackEntity

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.dhis2.data.search.SearchParametersModel
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchItem

interface SearchRepositoryKt {

    fun searchTrackedEntities(
        searchParametersModel: SearchParametersModel,
        isOnline: Boolean,
    ): Flow<PagingData<TrackedEntitySearchItem>>
}
