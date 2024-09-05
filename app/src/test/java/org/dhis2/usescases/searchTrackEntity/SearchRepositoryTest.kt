package org.dhis2.usescases.searchTrackEntity

import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.search.SearchParametersModel
import org.dhis2.form.ui.FieldViewModelFactory
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchCollectionRepository
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchItem
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SearchRepositoryTest {
    private val searchRepositoryJava: SearchRepository = mock()

    private val d2: D2 = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)

    private val dispatcher: DispatcherProvider = mock()

    private val fieldViewModelFactory: FieldViewModelFactory = mock()

    private val metadataIconProvider: MetadataIconProvider = mock()

    private lateinit var searchRepositoryImplKt: SearchRepositoryImplKt

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        searchRepositoryImplKt = SearchRepositoryImplKt(
            searchRepositoryJava,
            d2,
            dispatcher,
            fieldViewModelFactory,
            metadataIconProvider,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test searchTrackedEntities returns PagingData flow`() = runTest {
        val searchParametersModel: SearchParametersModel = SearchParametersModel(
            selectedProgram = null,
            queryData = null,
        )
        val trackedEntitySearchCollectionRepository: TrackedEntitySearchCollectionRepository =
            mock()
        val pagingDataFlow = flowOf(PagingData.empty<TrackedEntitySearchItem>())

        whenever(searchRepositoryJava.getFilteredRepository(searchParametersModel)) doReturn trackedEntitySearchCollectionRepository
        whenever(trackedEntitySearchCollectionRepository.allowOnlineCache()) doReturn mock()
        whenever(trackedEntitySearchCollectionRepository.allowOnlineCache().eq(false)) doReturn mock()
        whenever(trackedEntitySearchCollectionRepository.allowOnlineCache().eq(false).offlineFirst()) doReturn mock()
        whenever(trackedEntitySearchCollectionRepository.allowOnlineCache().eq(false).offlineOnly()) doReturn mock()
        whenever(trackedEntitySearchCollectionRepository.allowOnlineCache().eq(false).offlineOnly().getPagingData(10)) doReturn mock()
        whenever(trackedEntitySearchCollectionRepository.allowOnlineCache().eq(false).offlineFirst().getPagingData(10)) doReturn mock()
        whenever(trackedEntitySearchCollectionRepository.getPagingData(10)) doReturn pagingDataFlow

        val result =
            searchRepositoryImplKt.searchTrackedEntities(searchParametersModel, isOnline = true)

        result.collect { pagingData ->
            assertTrue(pagingData is PagingData<TrackedEntitySearchItem>)
        }

        verify(searchRepositoryJava).getFilteredRepository(searchParametersModel)
//        verify(trackedEntitySearchCollectionRepository).getPagingData(10)
    }
}
