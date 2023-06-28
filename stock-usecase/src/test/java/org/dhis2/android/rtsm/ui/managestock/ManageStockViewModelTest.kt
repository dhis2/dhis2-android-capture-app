package org.dhis2.android.rtsm.ui.managestock

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.github.javafaker.Faker
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.dhis2.android.rtsm.MainDispatcherRule
import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.android.rtsm.data.DestinationFactory
import org.dhis2.android.rtsm.data.FacilityFactory
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.data.models.IdentifiableModel
import org.dhis2.android.rtsm.data.models.StockItem
import org.dhis2.android.rtsm.data.models.Transaction
import org.dhis2.android.rtsm.services.SpeechRecognitionManager
import org.dhis2.android.rtsm.services.StockManager
import org.dhis2.android.rtsm.services.rules.RuleValidationHelper
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider
import org.dhis2.android.rtsm.services.scheduler.TrampolineSchedulerProvider
import org.dhis2.android.rtsm.ui.base.OnQuantityValidated
import org.dhis2.android.rtsm.utils.ParcelUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.hisp.dhis.rules.models.RuleEffect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class ManageStockViewModelTest {
    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var facility: IdentifiableModel
    private lateinit var distributedTo: IdentifiableModel
    private lateinit var transactionDate: String

    private val disposable = CompositeDisposable()
    private val faker = Faker()

    private lateinit var schedulerProvider: BaseSchedulerProvider
    private lateinit var appConfig: AppConfig

    @Mock
    private lateinit var ruleValidationHelperImpl: RuleValidationHelper

    @Mock
    private lateinit var speechRecognitionManagerImpl: SpeechRecognitionManager

    @Mock
    private lateinit var stockManager: StockManager

    private val resourceManager: ResourceManager = mock()
    private val tableModelMapper: TableModelMapper = mock()
    private val dispatcherProvider: DispatcherProvider = mock()

    private fun getModel() = ManageStockViewModel(
        disposable,
        schedulerProvider,
        stockManager,
        ruleValidationHelperImpl,
        speechRecognitionManagerImpl,
        resourceManager,
        tableModelMapper,
        dispatcherProvider
    )

    private fun createStockEntry(
        uid: String,
        viewModel: ManageStockViewModel,
        qty: String?
    ): StockItem {
        val stockItem = StockItem(
            uid,
            faker.name().name(),
            faker.number().numberBetween(1, 800).toString()
        )

        viewModel.addItem(stockItem, qty, stockItem.stockOnHand, null)

        return stockItem
    }

    @Before
    fun setUp() {
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

        facility = ParcelUtils.facilityToIdentifiableModelParcel(
            FacilityFactory.create(57L)
        )
        distributedTo = ParcelUtils.distributedTo_ToIdentifiableModelParcel(
            DestinationFactory.create(23L)
        )
        transactionDate = "2021-08-05"

        schedulerProvider = TrampolineSchedulerProvider()
    }

    @Test
    fun init_shouldSetFacilityDateAndDistributedToForDistribution() = runTest {
        val transaction = Transaction(
            transactionType = TransactionType.DISTRIBUTION,
            facility = facility,
            transactionDate = transactionDate,
            distributedTo = distributedTo
        )
        val viewModel = getModel()
        viewModel.setup(transaction)

        viewModel.transaction.let {
            assertNotNull(it.value?.facility)
            assertEquals(it.value?.facility?.displayName, facility.displayName)
            assertEquals(it.value?.distributedTo!!.displayName, distributedTo.displayName)
            assertEquals(it.value?.transactionDate, transactionDate)
        }
    }

    @Test
    fun init_shouldSetFacilityAndDateForDiscard() = runTest {
        val transaction = Transaction(
            transactionType = TransactionType.DISCARD,
            facility = facility,
            transactionDate = transactionDate,
            distributedTo = null
        )
        val viewModel = getModel()
        viewModel.setup(transaction)

        viewModel.transaction.let {
            assertNotNull(it.value?.facility)
            assertNull(it.value?.distributedTo)
            assertEquals(it.value?.facility?.displayName, facility.displayName)
            assertEquals(it.value?.transactionDate, transactionDate)
        }
    }

    @Test
    fun init_shouldSetFacilityAndDateForCorrection() = runTest {
        val transaction = Transaction(
            transactionType = TransactionType.CORRECTION,
            facility = facility,
            transactionDate = transactionDate,
            distributedTo = null
        )
        val viewModel = getModel()
        viewModel.setup(transaction)

        viewModel.transaction.let {
            assertNotNull(it.value?.facility)
            assertNull(it.value?.distributedTo)
            assertEquals(it.value?.facility?.displayName, facility.displayName)
            assertEquals(it.value?.transactionDate, transactionDate)
        }
    }

    @Test
    fun canSetAndGetItemQuantityForSelectedItem() {
        val viewModel = getModel()

        val qty = 319L
        val item = createStockEntry("someUid", viewModel, qty.toString())

        viewModel.setQuantity(
            item,
            200,
            qty.toString(),
            object : OnQuantityValidated {
                override fun validationCompleted(ruleEffects: List<RuleEffect>) {
                    Timber.tag("ruleEffects2").d("$ruleEffects")
                }
            }
        )

        assertEquals(viewModel.getItemQuantity(item)?.toLong(), qty)
    }

    @Test
    fun canUpdateExistingItemQuantityForSelectedItem() {
        val viewModel = getModel()
        val qty2 = 95L

        val item = createStockEntry("someUid", viewModel, qty2.toString())

        val qty = 49

        viewModel.setQuantity(
            item,
            0,
            qty.toString(),
            object : OnQuantityValidated {
                override fun validationCompleted(ruleEffects: List<RuleEffect>) {
                    println("$ruleEffects")
                }
            }
        )

        assertEquals(viewModel.getItemQuantity(item), qty2.toString())
    }
}
