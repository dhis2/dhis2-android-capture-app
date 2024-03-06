package dhis2.org.analytics.charts.providers

import dhis2.org.analytics.charts.ui.OrgUnitFilterType
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.datastore.KeyValuePair
import org.hisp.dhis.android.core.datastore.LocalDataStoreObjectRepository
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AnalyticsFilterProviderTest {

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val analyticsFilterProvider = AnalyticsFilterProvider(d2)

    @Test
    fun `Should not add org unit filter`() {
        val repo = mockLocalStore()
        analyticsFilterProvider.addOrgUnitFilter("uid", null, OrgUnitFilterType.NONE, listOf())
        verify(repo, times(0)).blockingSet(any())
        verify(repo, times(0)).blockingDeleteIfExist()
    }

    @Test
    fun `Should add all org unit filter`() {
        val repo = mockLocalStore()

        analyticsFilterProvider.addOrgUnitFilter("uid", null, OrgUnitFilterType.ALL, listOf())
        verify(repo).blockingSet(any())
        verify(repo).blockingDeleteIfExist()
    }

    @Test
    fun `Should add selection org unit filter`() {
        val repo = mockLocalStore()
        analyticsFilterProvider.addOrgUnitFilter("uid", null, OrgUnitFilterType.SELECTION, listOf())
        verify(repo, times(2)).blockingSet(any())
    }

    @Test
    fun `Should remove org unit filter`() {
        val repo = mockLocalStore()
        analyticsFilterProvider.removeOrgUnitFilter("uid", null)
        verify(repo, times(2)).blockingDeleteIfExist()
    }

    @Test
    fun `Should add period filter`() {
        val repo = mockLocalStore()
        analyticsFilterProvider.addPeriodFilter("uid", null, listOf())
        verify(repo).blockingSet(any())
    }

    @Test
    fun `Should remove period filter`() {
        val repo = mockLocalStore()
        analyticsFilterProvider.removePeriodFilter("uid", null)
        verify(repo).blockingDeleteIfExist()
    }

    @Test
    fun `Should return visualization periods if saved`() {
        val repo = mockValueSaved(true, KeyValuePair.builder().key("key").value("[TODAY]").build())
        analyticsFilterProvider.visualizationPeriod("any")
        verify(repo).blockingGet()
    }

    @Test
    fun `Should return null if visualization periods is not saved`() {
        val repo = mockValueSaved(false)
        val result = analyticsFilterProvider.visualizationPeriod("uid")
        assertTrue(result == null)
    }

    @Test
    fun `Should return visualization org unit type`() {
        val repo = mockValueSaved(true, KeyValuePair.builder().key("key").value("ALL").build())
        analyticsFilterProvider.visualizationOrgUnitsType("any")
        verify(repo).blockingGet()
    }

    @Test
    fun `Should return null visualization org unit type`() {
        val repo = mockValueSaved(false)
        val result = analyticsFilterProvider.visualizationOrgUnitsType("any")
        verify(repo, times(0)).blockingGet()
        assertTrue(result == null)
    }

    @Test
    fun `Should return visualization org units`() {
        val repo = mockValueSaved(true, KeyValuePair.builder().key("key").value("['uid']").build())
        analyticsFilterProvider.visualizationOrgUnits("any")
        verify(repo).blockingGet()
    }

    @Test
    fun `Should return null visualization org units`() {
        val repo = mockValueSaved(false)
        val result = analyticsFilterProvider.visualizationOrgUnits("any")
        verify(repo, times(0)).blockingGet()
        assertTrue(result == null)
    }

    private fun mockValueSaved(
        saved: Boolean,
        value: KeyValuePair? = null,
    ): LocalDataStoreObjectRepository {
        val repo: LocalDataStoreObjectRepository = mock()
        whenever(d2.dataStoreModule().localDataStore().value(any())) doReturn repo
        whenever(
            repo.blockingExists(),
        ) doReturn saved
        whenever(
            repo.blockingGet(),
        ) doReturn value
        return repo
    }

    private fun mockLocalStore(): LocalDataStoreObjectRepository {
        val repo: LocalDataStoreObjectRepository = mock()
        whenever(d2.dataStoreModule().localDataStore().value(any())) doReturn repo
        return repo
    }
}
