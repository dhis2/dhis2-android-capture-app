package org.dhis2.tracker.search.data

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.dhis2.tracker.input.ui.state.TrackerOptionItem

/**
 * Repository for fetching option set data.
 * Handles pagination and search of options for dropdown/selection inputs.
 */
interface OptionSetRepository {
    /**
     * Fetches paginated options for a given option set.
     *
     * @param optionSetUid The unique identifier of the option set
     * @param pageSize Number of items per page
     * @param searchQuery Optional search query to filter options
     * @return Flow of paginated option items
     */
    fun getOptions(
        optionSetUid: String,
        pageSize: Int = 10,
        searchQuery: String? = null,
    ): Flow<PagingData<TrackerOptionItem>>
}
