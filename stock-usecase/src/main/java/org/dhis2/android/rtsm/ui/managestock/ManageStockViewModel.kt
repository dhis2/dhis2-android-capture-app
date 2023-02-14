package org.dhis2.android.rtsm.ui.managestock

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.jakewharton.rxrelay2.PublishRelay
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import java.util.Collections
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.commons.Constants.QUANTITY_ENTRY_DEBOUNCE
import org.dhis2.android.rtsm.commons.Constants.SEARCH_QUERY_DEBOUNCE
import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.android.rtsm.data.ReviewStockData
import org.dhis2.android.rtsm.data.RowAction
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.data.models.SearchParametersModel
import org.dhis2.android.rtsm.data.models.StockEntry
import org.dhis2.android.rtsm.data.models.StockItem
import org.dhis2.android.rtsm.data.models.Transaction
import org.dhis2.android.rtsm.services.SpeechRecognitionManager
import org.dhis2.android.rtsm.services.StockManager
import org.dhis2.android.rtsm.services.rules.RuleValidationHelper
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider
import org.dhis2.android.rtsm.ui.base.OnQuantityValidated
import org.dhis2.android.rtsm.ui.base.SpeechRecognitionAwareViewModel
import org.dhis2.android.rtsm.ui.home.model.ButtonUiState
import org.dhis2.android.rtsm.ui.home.model.DataEntryStep
import org.dhis2.android.rtsm.ui.home.model.DataEntryUiState
import org.dhis2.android.rtsm.ui.home.model.SnackBarUiState
import org.dhis2.android.rtsm.utils.Utils.Companion.isValidStockOnHand
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.composetable.TableScreenState
import org.dhis2.composetable.model.KeyboardInputType
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TextInputModel
import org.hisp.dhis.rules.models.RuleActionAssign
import org.hisp.dhis.rules.models.RuleEffect
import org.jetbrains.annotations.NotNull

@HiltViewModel
class ManageStockViewModel @Inject constructor(
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    private val stockManagerRepository: StockManager,
    private val ruleValidationHelper: RuleValidationHelper,
    speechRecognitionManager: SpeechRecognitionManager,
    private val resources: ResourceManager,
    private val tableModelMapper: TableModelMapper
) : SpeechRecognitionAwareViewModel(
    schedulerProvider,
    speechRecognitionManager
) {
    private val _config = MutableLiveData<AppConfig>()
    val config: LiveData<AppConfig> = _config

    private val _transaction = MutableLiveData<Transaction?>()
    val transaction: LiveData<Transaction?> = _transaction

    private var search = MutableLiveData<SearchParametersModel>()
    private val searchRelay = PublishRelay.create<String>()
    private val entryRelay = PublishRelay.create<RowAction>()
    private val itemsCache = linkedMapOf<String, StockEntry>()

    private val _hasData = MutableStateFlow(false)
    val hasData = _hasData

    private val _screenState: MutableLiveData<TableScreenState> = MutableLiveData(
        TableScreenState(
            tables = emptyList(),
            selectNext = false,
            textInputCollapsedMode = false,
            overwrittenRowHeaderWidth = 200F
        )
    )
    val screenState: LiveData<TableScreenState> = _screenState

    private val _stockItems: MutableLiveData<List<StockItem>> =
        MutableLiveData<List<StockItem>>()

    private val _dataEntryUiState = MutableStateFlow(DataEntryUiState())
    val dataEntryUiState: StateFlow<DataEntryUiState> = _dataEntryUiState

    private val _themeColor = MutableStateFlow(Color.White)

    private val _scanText = MutableStateFlow("")
    val scanText = _scanText.asStateFlow()

    init {
        configureRelays()
    }

    fun setup(transaction: Transaction) {
        if (didTransactionParamsChange(transaction)) {
            _transaction.value = transaction
            updateStep(DataEntryStep.LISTING)
            loadStockItems()
            refreshData()
        }
    }

    private fun didTransactionParamsChange(transaction: Transaction): Boolean {
        return if (_transaction.value != null) {
            _transaction.value!!.transactionType != transaction.transactionType ||
                _transaction.value!!.facility != transaction.facility ||
                _transaction.value!!.distributedTo != transaction.distributedTo
        } else {
            true
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            getStockItems().asFlow().collect { stockItems ->
                _stockItems.value = stockItems
                populateTable()
            }
        }
    }

    fun setConfig(config: AppConfig) {
        _config.value = config
    }

    fun setThemeColor(themeColor: Color) {
        _themeColor.value = themeColor
    }

    private fun loadStockItems() {
        search.value = transaction.value?.facility?.uid?.let {
            SearchParametersModel(
                null, null,
                it
            )
        }
    }

    private fun getStockItems() = Transformations.switchMap(search) { q ->
        val result =
            stockManagerRepository.search(q, transaction.value?.facility?.uid, config.value!!)

        result.items
    }

    private fun configureRelays() {
        disposable.add(
            searchRelay
                .debounce(SEARCH_QUERY_DEBOUNCE, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { result ->
                        search.value =
                            transaction.value?.facility?.uid?.let {
                                SearchParametersModel(
                                    result, null,
                                    it
                                )
                            }
                    },
                    { it.printStackTrace() }
                )
        )

        disposable.add(
            entryRelay
                .debounce(QUANTITY_ENTRY_DEBOUNCE, TimeUnit.MILLISECONDS)
                .distinctUntilChanged { t1, t2 ->
                    t1.entry.item.id == t2.entry.item.id &&
                        t1.position == t2.position &&
                        t1.entry.qty == t2.entry.qty
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        disposable.add(
                            evaluate(
                                ruleValidationHelper,
                                it,
                                config.value?.program!!,
                                transaction.value!!,
                                Date(),
                                config.value!!
                            )
                        )
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    private fun populateTable(selectNext: Boolean = false) {
        val items = when (dataEntryUiState.value.step) {
            DataEntryStep.REVIEWING,
            DataEntryStep.EDITING_REVIEWING ->
                _stockItems.value?.filter {
                    itemsCache[it.id] != null
                }
            else -> _stockItems.value
        }

        val entries: List<StockEntry> = items?.map {
            itemsCache[it.id] ?: StockEntry(it)
        } ?: emptyList()

        _hasData.value = entries.isNotEmpty()

        val tables = tableModelMapper.map(
            entries = entries,
            stockLabel = resources.getString(R.string.stock),
            qtdLabel = provideQuantityLabel()
        )

        _screenState.postValue(
            TableScreenState(
                tables = tables,
                selectNext = selectNext,
                textInputCollapsedMode = false,
                overwrittenRowHeaderWidth = 200F
            )
        )
    }

    private fun provideQuantityLabel() = when (transaction.value?.transactionType) {
        TransactionType.CORRECTION -> resources.getString(R.string.count)
        else -> resources.getString(R.string.quantity)
    }

    private fun commitTransaction() {
        if (itemsCache.values.isEmpty()) {
            return
        }
        disposable.add(
            stockManagerRepository.saveTransaction(
                getPopulatedEntries(),
                transaction.value!!,
                config.value!!
            )
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        _dataEntryUiState.update { currentUiState ->
                            currentUiState.copy(
                                snackBarUiState = SnackBarUiState(
                                    message = R.string.transaction_completed,
                                    color = R.color.success_color,
                                    icon = R.drawable.success_icon
                                )
                            )
                        }
                        updateStep(DataEntryStep.COMPLETED)
                        cleanItemsFromCache()
                        clearTransaction()
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    fun onCellValueChanged(cell: TableCell) {
        val entries: List<StockEntry> = _stockItems.value?.map {
            itemsCache[it.id] ?: StockEntry(it)
        } ?: emptyList()
        val stockEntry = entries.find { it.item.id == cell.id }
        stockEntry?.let { entry ->
            addItem(entry.item, cell.value, entry.stockOnHand, false)
        }
        populateTable()
    }

    fun onCellClick(cell: TableCell, updateCellValue: (TableCell) -> Unit): TextInputModel {
        val stockItem = _stockItems.value?.find { it.id == cell.id }
        val itemName = stockItem?.name ?: ""
        return TextInputModel(
            id = cell.id ?: "",
            mainLabel = itemName,
            secondaryLabels = mutableListOf(resources.getString(R.string.quantity)),
            currentValue = cell.value,
            keyboardInputType = KeyboardInputType.NumericInput(
                allowDecimal = false,
                allowSigned = false
            )
        )
    }

    fun onSaveValueChange(
        cell: TableCell,
        selectNext: Boolean
    ) {
        populateTable(selectNext)
        viewModelScope.launch {
            saveValue(cell)
        }
    }

    private suspend fun saveValue(cell: TableCell) = withContext(Dispatchers.IO) {
        val stockItem = _stockItems.value?.find { it.id == cell.id }
        stockItem?.let {
            cell.value?.let { value ->
                setQuantity(
                    it, 0, value,
                    object : OnQuantityValidated {
                        override fun validationCompleted(ruleEffects: List<RuleEffect>) {
                            // When user taps on done or next. We should apply program rules here
                            ruleEffects.forEach { ruleEffect ->
                                if (ruleEffect.ruleAction() is RuleActionAssign &&
                                    (
                                        (ruleEffect.ruleAction() as RuleActionAssign).field()
                                            == config.value?.stockOnHand
                                        )
                                ) {
                                    val data = ruleEffect.data()
                                    val isValid: Boolean = isValidStockOnHand(data)
                                    val stockOnHand = if (isValid) data else it.stockOnHand
                                    addItem(it, cell.value, stockOnHand, !isValid)
                                }
                            }
                            populateTable()
                        }
                    }
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _scanText.value = query
        searchRelay.accept(query)
    }

    fun setQuantity(
        item: @NotNull StockItem,
        position: @NotNull Int,
        qty: @NotNull String,
        callback: OnQuantityValidated?
    ) {
        entryRelay.accept(RowAction(StockEntry(item, qty), position, callback))
    }

    fun getItemQuantity(item: StockItem): String? {
        return itemsCache[item.id]?.qty
    }

    fun addItem(item: StockItem, qty: String?, stockOnHand: String?, hasError: Boolean) {
        // Remove from cache any item whose quantity has been cleared
        if (qty.isNullOrEmpty()) {
            itemsCache.remove(item.id)
            hasUnsavedData(false)
            return
        }
        itemsCache[item.id] = StockEntry(item, qty, stockOnHand, hasError)
        hasUnsavedData(true)
    }

    fun cleanItemsFromCache() {
        hasUnsavedData(false)
        itemsCache.clear()
        updateReviewButton()
    }

    private fun hasUnsavedData(value: Boolean) {
        _dataEntryUiState.update { currentUiState ->
            currentUiState.copy(hasUnsavedData = value)
        }
    }

    private fun canReview(): Boolean = itemsCache.size > 0 && itemsCache.none { it.value.hasError }

    private fun getPopulatedEntries() = Collections.synchronizedList(itemsCache.values.toList())

    fun getData(): ReviewStockData = ReviewStockData(transaction.value!!, getPopulatedEntries())

    fun onEditingCell(isEditing: Boolean, onEditionStart: () -> Unit) {
        val step = when (dataEntryUiState.value.step) {
            DataEntryStep.LISTING -> if (isEditing) DataEntryStep.EDITING_LISTING else null
            DataEntryStep.EDITING_LISTING -> if (!isEditing) DataEntryStep.LISTING else null
            DataEntryStep.REVIEWING -> if (isEditing) DataEntryStep.EDITING_REVIEWING else null
            DataEntryStep.EDITING_REVIEWING -> if (!isEditing) DataEntryStep.REVIEWING else null
            else -> null
        }
        step?.let { updateStep(it) }

        if (isEditing) {
            onEditionStart.invoke()
        }
    }

    fun updateStep(step: DataEntryStep) {
        _dataEntryUiState.update { currentUiState ->
            currentUiState.copy(step = step)
        }
        updateReviewButton()
        populateTable()
    }

    private fun updateReviewButton() {
        val button: ButtonUiState = when (dataEntryUiState.value.step) {
            DataEntryStep.LISTING -> {
                val buttonVisibility = hasData.value && canReview()
                ButtonUiState(
                    text = R.string.review,
                    icon = R.drawable.proceed_icon,
                    contentColor = _themeColor.value,
                    containerColor = Color.White,
                    visible = buttonVisibility
                )
            }
            DataEntryStep.REVIEWING -> {
                val buttonVisibility = hasData.value && canReview()
                ButtonUiState(
                    text = R.string.confirm_transaction_label,
                    icon = R.drawable.confirm_review,
                    contentColor = Color.White,
                    containerColor = _themeColor.value,
                    visible = buttonVisibility
                )
            }
            else -> {
                dataEntryUiState.value.button.copy(visible = false)
            }
        }

        _dataEntryUiState.update { currentUiState ->
            currentUiState.copy(button = button)
        }
    }

    fun onButtonClick() {
        when (dataEntryUiState.value.step) {
            DataEntryStep.LISTING -> {
                onSearchQueryChanged("")
                updateStep(DataEntryStep.REVIEWING)
            }
            DataEntryStep.REVIEWING -> {
                commitTransaction()
            }
            else -> {
                // Nothing will happen given that the button is hidden
            }
        }
    }

    fun onHandleBackNavigation() {
        val backStep = when (dataEntryUiState.value.step) {
            DataEntryStep.REVIEWING -> {
                _hasData.value = false
                DataEntryStep.LISTING
            }
            else -> null
        }

        backStep?.let {
            updateStep(it)
        }
    }

    private fun clearTransaction() {
        _transaction.value = null
    }
}
