package org.dhis2.utils.filters

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.databinding.ObservableField
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import org.dhis2.R
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.Filters
import org.dhis2.commons.filters.ProgramType
import org.dhis2.commons.filters.SyncStateFilter
import org.dhis2.commons.filters.sorting.SortingItem
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.common.State
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

class SyncStateFilterTest {

    private val resourceManger: ResourceManager = mock()

    @Rule
    @JvmField
    var instantExecutorRule = InstantTaskExecutorRule()
    private val openFilter = ObservableField<Filters>()
    private val sortingItem = ObservableField<SortingItem>()
    lateinit var filterManager: FilterManager
    private lateinit var syncStateFilter: SyncStateFilter

    @Before
    fun setUp() {
        FilterManager.clearAll()
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        filterManager = FilterManager.initWith(resourceManger)
        filterManager.reset()
        syncStateFilter = SyncStateFilter(
            ProgramType.ALL,
            sortingItem,
            openFilter,
            "sync",
        )
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()
    }

    @Test
    fun `should return correct icon`() {
        assertTrue(syncStateFilter.icon() == R.drawable.ic_filter_sync)
    }

    @Test
    fun `Should add states to filter manager`() {
        syncStateFilter.setSyncStatus(true, State.TO_UPDATE, State.TO_POST, State.UPLOADING)
        assertTrue(
            filterManager.stateFilters.containsAll(
                listOf(
                    State.TO_UPDATE,
                    State.TO_POST,
                    State.UPLOADING,
                ),
            ),
        )
    }

    @Test
    fun `Should remove states to filter manager`() {
        syncStateFilter.setSyncStatus(true, State.TO_UPDATE, State.TO_POST, State.UPLOADING)
        syncStateFilter.setSyncStatus(true, State.SYNCED_VIA_SMS, State.SENT_VIA_SMS)
        syncStateFilter.setSyncStatus(false, State.SYNCED_VIA_SMS, State.SENT_VIA_SMS)
        assertTrue(
            filterManager.stateFilters.containsAll(
                listOf(
                    State.TO_UPDATE,
                    State.TO_POST,
                    State.UPLOADING,
                ),
            ) && !filterManager.stateFilters.containsAll(
                listOf(
                    State.SYNCED_VIA_SMS,
                    State.SENT_VIA_SMS,
                ),
            ),
        )
    }

    @Test
    fun `Should update observable`() {
        val testObservable = syncStateFilter.observeSyncState(State.TO_POST)
        assertTrue(!testObservable.get())
        syncStateFilter.setSyncStatus(true, State.TO_UPDATE, State.TO_POST, State.UPLOADING)
        assertTrue(testObservable.get())
    }
}
