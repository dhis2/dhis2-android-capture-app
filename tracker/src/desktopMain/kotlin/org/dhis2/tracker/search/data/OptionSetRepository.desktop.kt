package org.dhis2.tracker.search.data

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.dhis2.tracker.input.ui.state.TrackerOptionItem

/**
 * Desktop stub implementation of OptionSetRepository.
 * TODO: Implement when desktop support is added
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class OptionSetRepository {
    actual fun getOptions(
        optionSetUid: String,
        pageSize: Int,
        searchQuery: String?,
    ): Flow<PagingData<TrackerOptionItem>> {
        // Return empty flow for desktop
        return flowOf(PagingData.empty())
    }
}
