package org.dhis2.tracker.search.data

import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
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
class OptionSetRepositoryImpl(
    private val d2: D2,
    private val domainErrorMapper: DomainErrorMapper,
) : OptionSetRepository {
    /**
     * Fetches paginated options for a given option set.
     * @param optionSetUid The unique identifier of the option set
     * @param pageSize Number of items per page
     * @param searchQuery Optional search query to filter options
     * @return Flow of paginated option items. The `flow { }` builder creates a cold (lazy) flow that
     * will be executed when collected. The code inside runs in the collector's coroutine context.
     */
    override fun getOptions(
        optionSetUid: String,
        pageSize: Int,
        searchQuery: String?,
    ): Flow<PagingData<TrackerOptionItem>> =
        flow {
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

                val pagingFlow =
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

                emitAll(pagingFlow)
            } catch (d2Error: D2Error) {
                throw domainErrorMapper.mapToDomainError(d2Error)
            } catch (e: Exception) {
                throw DomainError.UnexpectedError(e.message ?: "Unknown error fetching options")
            }
        }
}
