package org.dhis2.android.rtsm.ui.managestock

import androidx.compose.runtime.State
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.jakewharton.rxrelay2.PublishRelay
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import java.util.Collections
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
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
import org.dhis2.composetable.TableScreenState
import org.dhis2.composetable.model.RowHeader
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableHeader
import org.dhis2.composetable.model.TableHeaderCell
import org.dhis2.composetable.model.TableHeaderRow
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.TableRowModel
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@HiltViewModel
class ManageStockViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    preferenceProvider: PreferenceProvider,
    private val stockManager: StockManager,
    private val ruleValidationHelper: RuleValidationHelper,
    speechRecognitionManager: SpeechRecognitionManager
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
    private val stockItems = Transformations.switchMap(search) { q ->
        _networkState.value = OperationState.Loading

        val result = stockManager.search(q, transaction.value?.facility?.uid, config.value!!)
        _itemsAvailableCount.value = result.totalCount

        _networkState.postValue(OperationState.Completed)
        result.items
    }
    private val itemsCache = linkedMapOf<String, StockEntry>()

    private val _networkState = MutableLiveData<OperationState<LiveData<PagedList<StockItem>>>>()
    val operationState: LiveData<OperationState<LiveData<PagedList<StockItem>>>>
        get() = _networkState

    fun setup(transaction: Transaction) {
        _transaction.value = transaction

        configureRelays()
        loadStockItems()
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

    fun getStockItems() = stockItems

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

    fun tableRowData(
        stockItems: State<PagedList<StockItem>?>,
        stockLabel: String,
        qtdLabel: String
    ): TableScreenState {
        val tableRowModels = mutableListOf<TableRowModel>()

        stockItems.value?.forEachIndexed { index, item ->
            val tableRowModel = TableRowModel(
                rowHeader = RowHeader(
                    id = item.id,
                    title = item.name,
                    row = index
                ),
                values = mapOf(
                    0 to TableCell(
                        id = item.id,
                        row = index,
                        column = 0,
                        editable = false,
                        value = item.stockOnHand
                    ),
                    1 to TableCell(
                        id = item.id,
                        row = index,
                        column = 1,
                        value = null,
                        editable = true
                    )
                ),
                maxLines = 3
            )

            tableRowModels.add(tableRowModel)
        }

        return TableScreenState(
            tables = mapTableModel(
                tableRowModels,
                stockLabel,
                qtdLabel
            ),
            selectNext = true
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
        callback: @Nullable ItemWatcher.OnQuantityValidated?
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
        if (qty == null) {
            itemsCache.remove(item.id)
            return
        }

        itemsCache[item.id] = StockEntry(item, qty, stockOnHand, hasError)
    }

    fun removeItemFromCache(item: StockItem) = itemsCache.remove(item.id) != null

    fun hasError(item: StockItem) = itemsCache[item.id]?.hasError ?: false

    fun canReview(): Boolean = itemsCache.size > 0 && itemsCache.none { it.value.hasError }

    private fun getPopulatedEntries() = Collections.synchronizedList(itemsCache.values.toList())

    fun getData(): ReviewStockData = ReviewStockData(transaction.value!!, getPopulatedEntries())

    fun getItemCount(): Int = itemsCache.size
}
