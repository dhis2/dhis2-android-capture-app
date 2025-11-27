package org.dhis2.android.rtsm.ui.managestock

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.liveData
import com.github.javafaker.Faker
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.dhis2.android.rtsm.MainDispatcherRule
import org.dhis2.android.rtsm.data.DestinationFactory
import org.dhis2.android.rtsm.data.FacilityFactory
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.data.models.IdentifiableModel
import org.dhis2.android.rtsm.data.models.SearchParametersModel
import org.dhis2.android.rtsm.data.models.SearchResult
import org.dhis2.android.rtsm.data.models.StockItem
import org.dhis2.android.rtsm.data.models.Transaction
import org.dhis2.android.rtsm.services.SpeechRecognitionManager
import org.dhis2.android.rtsm.services.StockManager
import org.dhis2.android.rtsm.services.StockTableDimensionStore
import org.dhis2.android.rtsm.services.rules.RuleValidationHelper
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider
import org.dhis2.android.rtsm.services.scheduler.TrampolineSchedulerProvider
import org.dhis2.android.rtsm.ui.base.OnQuantityValidated
import org.dhis2.android.rtsm.utils.ParcelUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.hisp.dhis.android.core.usecase.stock.StockUseCase
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
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import timber.log.Timber

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

    @Mock
    private lateinit var stockUseCase: StockUseCase

    @Mock
    private lateinit var ruleValidationHelperImpl: RuleValidationHelper

    @Mock
    private lateinit var speechRecognitionManagerImpl: SpeechRecognitionManager

    @Mock
    private lateinit var stockManager: StockManager

    private val resourceManager: ResourceManager = mock()
    private val tableModelMapper: TableModelMapper = mock()
    private val dispatcherProvider: DispatcherProvider = mock()
    private val stockTableDimensionStore: StockTableDimensionStore = mock()

    private fun getModel() =
        ManageStockViewModel(
            disposable,
            schedulerProvider,
            stockManager,
            ruleValidationHelperImpl,
            speechRecognitionManagerImpl,
            resourceManager,
            tableModelMapper,
            dispatcherProvider,
            stockTableDimensionStore,
            getStateHandle(),
        )

    private fun getStateHandle(): SavedStateHandle {
        val state =
            hashMapOf<String, Any>(
                org.dhis2.commons.Constants.PROGRAM_UID to "F5ijs28K4s8",
            )
        return SavedStateHandle(state)
    }

    private fun createStockEntry(
        uid: String,
        viewModel: ManageStockViewModel,
        qty: String?,
    ): StockItem {
        val stockItem =
            StockItem(
                uid,
                faker.name().name(),
                faker.number().numberBetween(1, 800).toString(),
            )

        viewModel.addItem(stockItem, qty, stockItem.stockOnHand, null)

        return stockItem
    }

    @Before
    fun setUp() {
        stockUseCase =
            StockUseCase(
                programUid = "F5ijs28K4s8",
                description = "Paracetamol",
                itemDescription = "sLMTQUHAZnk",
                itemCode = "wBr4wccNBj1",
                programType = "LMIS",
                stockOnHand = "RghnAkDBDI4",
                transactions = emptyList(),
            )

        facility =
            ParcelUtils.facilityToIdentifiableModelParcel(
                FacilityFactory.create(57L),
            )
        distributedTo =
            ParcelUtils.distributedTo_ToIdentifiableModelParcel(
                DestinationFactory.create(23L),
            )
        transactionDate = "2021-08-05"

        schedulerProvider = TrampolineSchedulerProvider()

        runBlocking {
            whenever(stockManager.stockUseCase(stockUseCase.programUid))
                .thenReturn(stockUseCase)
        }
    }

    @Test
    fun init_shouldSetFacilityDateAndDistributedToForDistribution() =
        runTest {
            whenever(
                stockManager.search(
                    query =
                        SearchParametersModel(
                            null,
                            null,
                            facility.uid,
                        ),
                    ou = facility.uid,
                    config = stockUseCase,
                ),
            ) doReturn SearchResult(liveData { emptyList<StockItem>() })

            val transaction =
                Transaction(
                    transactionType = TransactionType.DISTRIBUTION,
                    facility = facility,
                    transactionDate = transactionDate,
                    distributedTo = distributedTo,
                )
            val viewModel = getModel()
            viewModel.setConfig(stockUseCase.programUid)
            viewModel.setup(transaction)

            viewModel.transaction.let {
                assertNotNull(it.value?.facility)
                assertEquals(it.value?.facility?.displayName, facility.displayName)
                assertEquals(it.value?.distributedTo!!.displayName, distributedTo.displayName)
                assertEquals(it.value?.transactionDate, transactionDate)
            }
        }

    @Test
    fun init_shouldSetFacilityAndDateForDiscard() =
        runTest {
            whenever(
                stockManager.search(
                    query =
                        SearchParametersModel(
                            null,
                            null,
                            facility.uid,
                        ),
                    ou = facility.uid,
                    config = stockUseCase,
                ),
            ) doReturn SearchResult(liveData { emptyList<StockItem>() })

            val transaction =
                Transaction(
                    transactionType = TransactionType.DISCARD,
                    facility = facility,
                    transactionDate = transactionDate,
                    distributedTo = null,
                )
            val viewModel = getModel()
            viewModel.setConfig(stockUseCase.programUid)
            viewModel.setup(transaction)

            viewModel.transaction.let {
                assertNotNull(it.value?.facility)
                assertNull(it.value?.distributedTo)
                assertEquals(it.value?.facility?.displayName, facility.displayName)
                assertEquals(it.value?.transactionDate, transactionDate)
            }
        }

    @Test
    fun init_shouldSetFacilityAndDateForCorrection() =
        runTest {
            whenever(
                stockManager.search(
                    query =
                        SearchParametersModel(
                            null,
                            null,
                            facility.uid,
                        ),
                    ou = facility.uid,
                    config = stockUseCase,
                ),
            ) doReturn SearchResult(liveData { emptyList<StockItem>() })

            val transaction =
                Transaction(
                    transactionType = TransactionType.CORRECTION,
                    facility = facility,
                    transactionDate = transactionDate,
                    distributedTo = null,
                )
            val viewModel = getModel()
            viewModel.setConfig(stockUseCase.programUid)
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
            },
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
                    // no-op
                }
            },
        )

        assertEquals(viewModel.getItemQuantity(item), qty2.toString())
    }
}
