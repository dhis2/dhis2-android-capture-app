package org.dhis2.android.rtsm.ui.managestock

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.jakewharton.rxrelay2.PublishRelay
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import java.util.Collections
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.commons.Constants.QUANTITY_ENTRY_DEBOUNCE
import org.dhis2.android.rtsm.commons.Constants.SEARCH_QUERY_DEBOUNCE
import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.android.rtsm.data.OperationState
import org.dhis2.android.rtsm.data.ReviewStockData
import org.dhis2.android.rtsm.data.RowAction
import org.dhis2.android.rtsm.data.models.SearchParametersModel
import org.dhis2.android.rtsm.data.models.StockEntry
import org.dhis2.android.rtsm.data.models.StockItem
import org.dhis2.android.rtsm.data.models.Transaction
import org.dhis2.android.rtsm.services.SpeechRecognitionManager
import org.dhis2.android.rtsm.services.StockManager
import org.dhis2.android.rtsm.services.preferences.PreferenceProvider
import org.dhis2.android.rtsm.services.rules.RuleValidationHelper
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider
import org.dhis2.android.rtsm.ui.base.ItemWatcher
import org.dhis2.android.rtsm.ui.base.SpeechRecognitionAwareViewModel
import org.dhis2.android.rtsm.ui.home.model.ButtonUiState
import org.dhis2.android.rtsm.ui.home.model.ButtonVisibilityState
import org.dhis2.android.rtsm.utils.Utils.Companion.isValidStockOnHand
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.composetable.TableScreenState
import org.dhis2.composetable.model.KeyboardInputType
import org.dhis2.composetable.model.RowHeader
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableHeader
import org.dhis2.composetable.model.TableHeaderCell
import org.dhis2.composetable.model.TableHeaderRow
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.TableRowModel
import org.dhis2.composetable.model.TextInputModel
import org.hisp.dhis.rules.models.RuleActionAssign
import org.hisp.dhis.rules.models.RuleEffect
import org.jetbrains.annotations.NotNull

@HiltViewModel
class ManageStockViewModel @Inject constructor(
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    preferenceProvider: PreferenceProvider,
    private val stockManager: StockManager,
    private val ruleValidationHelper: RuleValidationHelper,
    speechRecognitionManager: SpeechRecognitionManager,
    private val resources: ResourceManager
) : SpeechRecognitionAwareViewModel(
    preferenceProvider,
    schedulerProvider,
    speechRecognitionManager
) {
    private val _config = MutableLiveData<AppConfig>()
    val config: LiveData<AppConfig> = _config

    private val _transaction = MutableLiveData<Transaction>()
    val transaction: LiveData<Transaction?> = _transaction

    private val _itemsAvailableCount = MutableLiveData<Int>(0)
    private var search = MutableLiveData<SearchParametersModel>()
    private val searchRelay = PublishRelay.create<String>()
    private val entryRelay = PublishRelay.create<RowAction>()
    private val itemsCache = linkedMapOf<String, StockEntry>()

    private val _hasData = MutableStateFlow(false)
    val hasData = _hasData

    private val _networkState = MutableLiveData<OperationState<LiveData<PagedList<StockItem>>>>()
    val operationState: LiveData<OperationState<LiveData<PagedList<StockItem>>>>
        get() = _networkState

    private val _allTableState = MutableStateFlow<List<TableModel>>(mutableListOf())
    private val allTableState: StateFlow<List<TableModel>> = _allTableState

    private val _screenState: MutableLiveData<TableScreenState> = MutableLiveData(
        TableScreenState(emptyList(), false)
    )
    val screenState: LiveData<TableScreenState> = _screenState

    private val _stockItems: MutableLiveData<PagedList<StockItem>> =
        MutableLiveData<PagedList<StockItem>>()

    private val _reviewButtonUiState = MutableStateFlow(ButtonUiState())
    val reviewButtonUiState: StateFlow<ButtonUiState> = _reviewButtonUiState

    fun setup(transaction: Transaction) {
        _transaction.value = transaction

        configureRelays()
        loadStockItems()
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            getStockItems().asFlow().collect {
                _stockItems.value = it
                tableRowData(
                    it,
                    resources.getString(R.string.stock),
                    resources.getString(R.string.quantity)
                )
            }
        }
    }

    fun setConfig(config: AppConfig) {
        _config.value = config
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
        _networkState.value = OperationState.Loading

        val result = stockManager.search(q, transaction.value?.facility?.uid, config.value!!)
        _itemsAvailableCount.value = result.totalCount

        _networkState.postValue(OperationState.Completed)
        result.items
    }

    fun getAvailableCount(): LiveData<Int> = _itemsAvailableCount

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

    private fun tableRowData(
        stockItems: PagedList<StockItem>?,
        stockLabel: String,
        qtdLabel: String
    ) {
        val tableRowModels = mutableListOf<TableRowModel>()

        _hasData.value = stockItems!!.size > 0

        stockItems?.forEachIndexed { index, item ->
            val tableRowModel = TableRowModel(
                rowHeader = RowHeader(
                    id = item.id,
                    title = item.name,
                    row = index
                ),
                values = mapOf(
                    Pair(
                        0,
                        TableCell(
                            id = item.id,
                            row = index,
                            column = 0,
                            editable = false,
                            value = item.stockOnHand
                        )
                    ),
                    Pair(
                        1,
                        TableCell(
                            id = item.id,
                            row = index,
                            column = 1,
                            value = null,
                            editable = true
                        )
                    )
                ),
                maxLines = 3
            )

            tableRowModels.add(tableRowModel)
        }

        _allTableState.value = mapTableModel(
            tableRowModels,
            stockLabel,
            qtdLabel
        )

        _screenState.value = TableScreenState(
            tables = allTableState.value,
            selectNext = false
        )
    }

    private fun mapTableModel(
        stocks: List<TableRowModel>,
        stockLabel: String,
        qtdLabel: String
    ) = mutableListOf(
        TableModel(
            id = "STOCK",
            tableHeaderModel = TableHeader(
                rows = mutableListOf(
                    TableHeaderRow(
                        mutableListOf(
                            TableHeaderCell(stockLabel),
                            TableHeaderCell(qtdLabel)
                        )
                    )
                )
            ),
            tableRows = stocks
        )
    )

    fun onCellValueChanged(tableCell: TableCell) {
        val updatedData = screenState.value?.tables?.map { tableModel ->
            if (tableModel.hasCellWithId(tableCell.id)) {
                tableModel.copy(
                    overwrittenValues = mapOf(
                        Pair(tableCell.column!!, tableCell)
                    )
                )
            } else {
                tableModel
            }
        } ?: emptyList()

        _allTableState.value = updatedData

        _screenState.postValue(
            TableScreenState(
                tables = allTableState.value,
                selectNext = false
            )
        )
    }

    fun onCellClick(cell: TableCell): TextInputModel {
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
        // When user taps on done or next. We should apply program rules here
        val stockItem = _stockItems.value?.find { it.id == cell.id }
        stockItem?.let {
            cell.value?.let { value ->
                setQuantity(
                    it, 0, value,
                    object : ItemWatcher.OnQuantityValidated {
                        override fun validationCompleted(ruleEffects: List<RuleEffect>) {
                            // Update fields
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

                                    _allTableState.value = _allTableState.value.map { tableModel ->
                                        tableModel.copy(
                                            tableRows = updateTableRows(tableModel.tableRows, cell)
                                        )
                                    }
                                }
                            }

                            _screenState.postValue(
                                TableScreenState(
                                    tables = allTableState.value,
                                    selectNext = selectNext
                                )
                            )
                        }
                    }
                )
            }
        }
    }

    private fun updateTableRows(
        tableRowModels: List<TableRowModel>,
        cell: TableCell
    ): List<TableRowModel> {
        return tableRowModels.map { tableRowModel ->
            if (tableRowModel.values.values.find { tableCell ->
                tableCell.id == cell.id
            } != null
            ) {
                tableRowModel.copy(
                    values = updateTableCells(tableRowModel.values, cell)
                )
            } else {
                tableRowModel
            }
        }
    }

    private fun updateTableCells(
        tableCells: Map<Int, TableCell>,
        cell: TableCell
    ): Map<Int, TableCell> {
        val stockEntry = getPopulatedEntries().find { it.item.id == cell.id }
        return tableCells.mapValues { (index, tableCell) ->
            when (index) {
                0 -> tableCell.copy(
                    value = stockEntry?.stockOnHand
                )
                else -> tableCell.copy(
                    value = stockEntry?.qty,
                    error = if (stockEntry?.hasError == true) {
                        resources.getString(R.string.stock_on_hand_exceeded_message)
                    } else {
                        null
                    }
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchRelay.accept(query)
    }

    fun onScanCompleted(itemCode: String) {
        search.postValue(SearchParametersModel(null, itemCode, transaction.value?.facility?.uid!!))
    }

    fun setQuantity(
        item: @NotNull StockItem,
        position: @NotNull Int,
        qty: @NotNull String,
        callback: ItemWatcher.OnQuantityValidated?
    ) {
        entryRelay.accept(RowAction(StockEntry(item, qty), position, callback))
    }

    fun getItemQuantity(item: StockItem): String? {
        println(itemsCache)
        return itemsCache[item.id]?.qty
    }

    fun getStockOnHand(item: StockItem) = itemsCache[item.id]?.stockOnHand

    fun addItem(item: StockItem, qty: String?, stockOnHand: String?, hasError: Boolean) {
        // Remove from cache any item whose quantity has been cleared
        if (qty.isNullOrEmpty()) {
            itemsCache.remove(item.id)
            return
        }

        itemsCache[item.id] = StockEntry(item, qty, stockOnHand, hasError)
    }

    fun removeItemFromCache(item: StockItem) = itemsCache.remove(item.id) != null

    fun hasError(item: StockItem) = itemsCache[item.id]?.hasError ?: false

    private fun canReview(): Boolean = itemsCache.size > 0 && itemsCache.none { it.value.hasError }

    private fun getPopulatedEntries() = Collections.synchronizedList(itemsCache.values.toList())

    fun getData(): ReviewStockData = ReviewStockData(transaction.value!!, getPopulatedEntries())

    fun getItemCount(): Int = itemsCache.size
    fun onEditingCell(isEditing: Boolean, onEditionStart: () -> Unit) {
        updateReviewButton(isEditing)
        if (isEditing) {
            onEditionStart.invoke()
        }
    }

    private fun updateReviewButton(isEditing: Boolean = false) {
        val buttonState: ButtonVisibilityState = if (isEditing || !hasData.value) {
            ButtonVisibilityState.HIDDEN
        } else {
            if (canReview()) {
                ButtonVisibilityState.ENABLED
            } else {
                ButtonVisibilityState.DISABLED
            }
        }

        _reviewButtonUiState.update { currentUiState ->
            currentUiState.copy(visibility = buttonState)
        }
    }
}
