package org.dhis2.tracker.search.data

import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.commons.error.DomainErrorMapper
import org.dhis2.tracker.input.ui.state.TrackerOptionItem
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.maintenance.D2Error

/**
 * Android implementation of OptionSetRepository using DHIS2 Android SDK.
 */
actual class OptionSetRepository(
    private val d2: D2,
    private val domainErrorMapper: DomainErrorMapper,
) {
    actual suspend fun getOptions(
        optionSetUid: String,
        pageSize: Int,
        searchQuery: String?,
    ): Flow<PagingData<TrackerOptionItem>> =
        try {
            var query =
                d2
                    .optionModule()
                    .options()
                    .byOptionSetUid()
                    .eq(optionSetUid)
                    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)

            // Apply search filter if provided
            searchQuery?.takeIf { it.isNotBlank() }?.let { search ->
                query = query.byDisplayName().like("%$search%")
            }

            query
                .getPagingData(pageSize)
                .map { pagingData ->
                    pagingData.map { option ->
                        TrackerOptionItem(
                            code = option.code() ?: "",
                            displayName = option.displayName() ?: "",
                        )
                    }
                }
        } catch (d2Error: D2Error) {
            throw domainErrorMapper.mapToDomainError(d2Error)
        } catch (e: Exception) {
            throw DomainError.UnexpectedError(e.message ?: "Unknown error fetching options")
        }
}
