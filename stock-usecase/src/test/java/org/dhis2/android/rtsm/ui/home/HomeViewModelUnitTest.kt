package org.dhis2.android.rtsm.ui.home

import androidx.arch.core.executor.testing.CountingTaskExecutorRule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import java.time.LocalDateTime
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.setMain
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.commons.Constants
import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.android.rtsm.data.DestinationFactory
import org.dhis2.android.rtsm.data.FacilityFactory
import org.dhis2.android.rtsm.data.OperationState
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.exceptions.UserIntentParcelCreationException
import org.dhis2.android.rtsm.services.MetadataManager
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider
import org.dhis2.android.rtsm.services.scheduler.TrampolineSchedulerProvider
import org.dhis2.android.rtsm.utils.ParcelUtils
import org.dhis2.android.rtsm.utils.humanReadableDate
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class HomeViewModelUnitTest {
    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val countingTaskExecutorRule = CountingTaskExecutorRule()

    @Mock
    private lateinit var metadataManager: MetadataManager

    private lateinit var viewModel: HomeViewModel
    private lateinit var schedulerProvider: BaseSchedulerProvider

    @OptIn(ExperimentalCoroutinesApi::class)
    @Mock
    private lateinit var testSchedulerProvider: TestCoroutineScheduler
    private lateinit var facilities: List<OrganisationUnit>
    private lateinit var destinations: List<Option>
    private lateinit var appConfig: AppConfig

    private val disposable = CompositeDisposable()

    @Mock
    private lateinit var facilitiesObserver: Observer<OperationState<List<OrganisationUnit>>>

    @Mock
    private lateinit var destinationsObserver: Observer<OperationState<List<Option>>>

    @OptIn(DelicateCoroutinesApi::class)
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(mainThreadSurrogate)
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        appConfig = AppConfig(
            "F5ijs28K4s8",
            "wBr4wccNBj1",
            "sLMTQUHAZnk",
            "RghnAkDBDI4",
            "yfsEseIcEXr",
            "lpGYJoVUudr",
            "ej1YwWaYGmm",
            "I7cmT3iXT0y"
        )

        facilities = FacilityFactory.getListOf(3)
        destinations = DestinationFactory.getListOf(5)

        schedulerProvider = TrampolineSchedulerProvider()
        testSchedulerProvider = TestCoroutineScheduler()

        doReturn(
            Single.just(facilities)
        ).whenever(metadataManager).facilities(appConfig.program)

        `when`(metadataManager.destinations(appConfig.distributedTo))
            .thenReturn(Single.just(destinations))

        viewModel = HomeViewModel(
            disposable,
            schedulerProvider,
            metadataManager,
            getStateHandle()
        )

        viewModel.facilities.asLiveData().observeForever(facilitiesObserver)
        viewModel.destinationsList.asLiveData().observeForever(destinationsObserver)
    }

    private fun getStateHandle(): SavedStateHandle {
        val state = hashMapOf<String, Any>(
            Constants.INTENT_EXTRA_APP_CONFIG to appConfig
        )
        return SavedStateHandle(state)
    }

    @After
    fun tearDown() {
        disposable.dispose()
        RxJavaPlugins.reset()
        RxAndroidPlugins.reset()
    }

    @Test
    fun init_shouldLoadFacilities() {
        verify(metadataManager).facilities(appConfig.program)

        assertEquals(viewModel.facilities.value, OperationState.Success(facilities))
    }

    @Test
    fun init_shouldLoadDestinations() {
        verify(metadataManager).destinations(appConfig.distributedTo)

        assertEquals(viewModel.destinationsList.value, OperationState.Success(destinations))
    }

    @Test
    fun canSelectDifferentTransactionTypes() {
        val types = listOf<TransactionType>(
            TransactionType.DISTRIBUTION,
            TransactionType.DISCARD,
            TransactionType.CORRECTION
        )

        types.forEach {
            viewModel.selectTransaction(it)
            assertEquals(viewModel.settingsUiState.value.transactionType, it)
        }
    }

    @Test
    fun isDistributionIsPositive_whenDistributionIsSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        assertEquals(viewModel.settingsUiState.value.transactionType, TransactionType.DISTRIBUTION)
    }

    @Test
    fun isDistributionIsNegative_whenDiscardIsSet() {
        viewModel.selectTransaction(TransactionType.DISCARD)
        assertNotEquals(
            viewModel.settingsUiState.value.transactionType,
            TransactionType.DISTRIBUTION
        )
    }

    @Test
    fun isDistributionIsNegative_whenCorrectionIsSet() {
        viewModel.selectTransaction(TransactionType.CORRECTION)
        assertNotEquals(
            viewModel.settingsUiState.value.transactionType,
            TransactionType.DISTRIBUTION
        )
    }

    @Test
    fun distributionTransaction_cannotManageStock_ifNoParametersAreSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        assertEquals(viewModel.checkForFieldErrors(), R.string.mandatory_facility_selection)
    }

    @Test
    fun distributionTransaction_cannotManageStock_ifOnlyFacilityIsSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setFacility(facilities[0])
        assertEquals(viewModel.checkForFieldErrors(), R.string.mandatory_distributed_to_selection)
    }

    @Test
    fun distributionTransaction_cannotManageStock_ifOnlyTransactionDateIsSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        assertEquals(viewModel.checkForFieldErrors(), R.string.mandatory_facility_selection)
    }

    @Test
    fun distributionTransaction_cannotManageStock_ifOnlyDistributedToIsSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setDestination(destinations[0])
        assertEquals(viewModel.checkForFieldErrors(), R.string.mandatory_facility_selection)
    }

    @Test
    fun distributionTransaction_cannotManageStock_ifOnlyFacilityAndTransactionDateIsSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setFacility(facilities[0])
        assertEquals(viewModel.checkForFieldErrors(), R.string.mandatory_distributed_to_selection)
    }

    @Test
    fun distributionTransaction_cannotManageStock_ifOnlyDestinedToAndTransactionDateIsSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setDestination(destinations[0])
        assertEquals(viewModel.checkForFieldErrors(), R.string.mandatory_facility_selection)
    }

    @Test
    fun distributionTransaction_cannotManageStock_ifOnlyFacilityAndDestinedToIsSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setFacility(facilities[0])
        viewModel.setDestination(destinations[0])
        assertEquals(viewModel.checkForFieldErrors(), null)
    }

    @Test
    fun distributionTransaction_canManageStock_ifAllFieldsAreSet() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setFacility(facilities[0])
        viewModel.setDestination(destinations[0])
        assertEquals(viewModel.checkForFieldErrors(), null)
    }

    @Test
    fun discardTransaction_cannotManageStock_ifNoParametersAreSet() {
        viewModel.selectTransaction(TransactionType.DISCARD)
        assertEquals(viewModel.checkForFieldErrors(), R.string.mandatory_facility_selection)
    }

    @Test
    fun discardTransaction_cannotManageStock_ifOnlyFacilityIsSet() {
        viewModel.selectTransaction(TransactionType.DISCARD)
        viewModel.setFacility(facilities[0])
        assertEquals(viewModel.checkForFieldErrors(), null)
    }

    @Test
    fun discardTransaction_cannotManageStock_ifOnlyTransactionDateIsSet() {
        viewModel.selectTransaction(TransactionType.DISCARD)

        assertEquals(viewModel.checkForFieldErrors(), R.string.mandatory_facility_selection)
    }

    @Test
    fun discardTransaction_canManageStock_ifAllFieldsAreSet() {
        viewModel.selectTransaction(TransactionType.DISCARD)
        viewModel.setFacility(facilities[0])

        assertEquals(viewModel.checkForFieldErrors(), null)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun discardTransaction_throwsErrorIfDistributedToIsSet() {
        viewModel.selectTransaction(TransactionType.DISCARD)
        viewModel.setDestination(destinations[1])
    }

    @Test
    fun correctionTransaction_cannotManageStock_ifNoParametersAreSet() {
        viewModel.selectTransaction(TransactionType.CORRECTION)
        assertEquals(viewModel.checkForFieldErrors(), R.string.mandatory_facility_selection)
    }

    @Test
    fun correctionTransaction_cannotManageStock_ifOnlyFacilityIsSet() {
        viewModel.selectTransaction(TransactionType.CORRECTION)
        viewModel.setFacility(facilities[0])
        assertEquals(viewModel.checkForFieldErrors(), null)
    }

    @Test
    fun correctionTransaction_cannotManageStock_ifOnlyTransactionDateIsSet() {
        viewModel.selectTransaction(TransactionType.CORRECTION)

        assertEquals(viewModel.checkForFieldErrors(), R.string.mandatory_facility_selection)
    }

    @Test
    fun correctionTransaction_canManageStock_ifAllFieldsAreSet() {
        viewModel.selectTransaction(TransactionType.CORRECTION)
        viewModel.setFacility(facilities[0])

        assertEquals(viewModel.checkForFieldErrors(), null)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun correctionTransaction_throwsErrorIfDistributedToIsSet() {
        viewModel.selectTransaction(TransactionType.CORRECTION)
        viewModel.setDestination(destinations[1])
    }

    @Test(expected = UserIntentParcelCreationException::class)
    fun distributionWithMissingFacility_cannotCreateUserIntent() {
        viewModel.selectTransaction(TransactionType.DISTRIBUTION)

        viewModel.getData()
    }

    @Test
    fun distributionWithMissingTransactionDate_willCreateUSerIntentWithCurrentDay() {
        val now = LocalDateTime.now()

        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setFacility(facilities[1])

        val data = viewModel.getData()
        assertEquals(data.transactionDate, now.humanReadableDate())
    }

    @Test
    fun distributionWithCompleteInformation_canCreateUserIntent() {
        val destination = destinations[2]
        val facility = facilities[1]
        val now = LocalDateTime.now()

        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setDestination(destination)
        viewModel.setFacility(facility)

        val data = viewModel.getData()
        assertEquals(data.transactionType, TransactionType.DISTRIBUTION)
        assertEquals(
            data.facility,
            ParcelUtils.facilityToIdentifiableModelParcel(facility)
        )
        assertEquals(
            data.distributedTo,
            ParcelUtils.distributedTo_ToIdentifiableModelParcel(destination)
        )
        println(data)
        assertEquals(data.transactionDate, now.humanReadableDate())
    }

    @Test
    fun discardWithCompleteInformation_canCreateUserIntent() {
        val facility = facilities[1]
        val now = LocalDateTime.now()

        viewModel.selectTransaction(TransactionType.DISCARD)
        viewModel.setFacility(facility)

        val data = viewModel.getData()
        assertEquals(data.transactionType, TransactionType.DISCARD)
        assertEquals(
            data.facility,
            ParcelUtils.facilityToIdentifiableModelParcel(facility)
        )
        assertEquals(data.transactionDate, now.humanReadableDate())
    }

    @Test
    fun correctionWithCompleteInformation_canCreateUserIntent() {
        val facility = facilities[1]
        val now = LocalDateTime.now()

        viewModel.selectTransaction(TransactionType.CORRECTION)
        viewModel.setFacility(facility)

        val data = viewModel.getData()
        assertEquals(data.transactionType, TransactionType.CORRECTION)
        assertEquals(
            data.facility,
            ParcelUtils.facilityToIdentifiableModelParcel(facility)
        )
        assertEquals(data.transactionDate, now.humanReadableDate())
    }

    @Test
    fun shouldChangeToolbarSubtitle_forDistribution() {
        val destination = destinations[2]
        val facility = facilities[1]

        viewModel.selectTransaction(TransactionType.DISTRIBUTION)
        viewModel.setDestination(destination)
        viewModel.setFacility(facility)

        assertNotNull(viewModel.settingsUiState.value.fromFacilitiesLabel())
        assertNotNull(viewModel.settingsUiState.value.deliverToLabel())
    }

    @Test
    fun shouldChangeToolbarSubtitle_forDiscard() {
        val facility = facilities[1]

        viewModel.selectTransaction(TransactionType.DISCARD)
        viewModel.setFacility(facility)

        assertNotNull(viewModel.settingsUiState.value.fromFacilitiesLabel())
    }

    @Test
    fun shouldChangeToolbarSubtitle_forCorrection() {
        val facility = facilities[1]

        viewModel.selectTransaction(TransactionType.CORRECTION)
        viewModel.setFacility(facility)

        assertNotNull(viewModel.settingsUiState.value.fromFacilitiesLabel())
    }
}
