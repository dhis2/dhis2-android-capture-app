package org.dhis2.android.rtsm.ui.managestock

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.jakewharton.rxrelay2.PublishRelay
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.android.rtsm.commons.Constants
import org.dhis2.android.rtsm.commons.Constants.INTENT_EXTRA_TRANSACTION
import org.dhis2.android.rtsm.commons.Constants.QUANTITY_ENTRY_DEBOUNCE
import org.dhis2.android.rtsm.commons.Constants.SEARCH_QUERY_DEBOUNCE
import org.dhis2.android.rtsm.data.*
import org.dhis2.android.rtsm.data.models.SearchParametersModel
import org.dhis2.android.rtsm.data.models.StockEntry
import org.dhis2.android.rtsm.data.models.StockItem
import org.dhis2.android.rtsm.data.models.Transaction
import org.dhis2.android.rtsm.exceptions.InitializationException
import org.dhis2.android.rtsm.services.SpeechRecognitionManager
import org.dhis2.android.rtsm.services.StockManager
import org.dhis2.android.rtsm.services.preferences.PreferenceProvider
import org.dhis2.android.rtsm.services.rules.RuleValidationHelper
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider
import org.dhis2.android.rtsm.ui.base.ItemWatcher
import org.dhis2.android.rtsm.ui.base.SpeechRecognitionAwareViewModel
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.util.Date
import java.util.Collections
import java.util.concurrent.TimeUnit
import javax.inject.Inject

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
    val transaction: Transaction = savedState.get<Transaction>(INTENT_EXTRA_TRANSACTION)
        ?: throw InitializationException("Transaction information is missing")

    val config: AppConfig = savedState.get<AppConfig>(Constants.INTENT_EXTRA_APP_CONFIG)
        ?: throw InitializationException("Some configuration parameters are missing")

    private val _itemsAvailableCount = MutableLiveData<Int>(0)
    private var search = MutableLiveData<SearchParametersModel>()
    private val searchRelay = PublishRelay.create<String>()
    private val entryRelay = PublishRelay.create<RowAction>()
    private val stockItems = Transformations.switchMap(search) { q ->
        _networkState.value = OperationState.Loading

        val result = stockManager.search(q, transaction.facility.uid, config)
        _itemsAvailableCount.value = result.totalCount

        _networkState.postValue(OperationState.Completed)
        result.items
    }
    private val itemsCache = linkedMapOf<String, StockEntry>()

    private val _networkState = MutableLiveData<OperationState<LiveData<PagedList<StockItem>>>>()
    val operationState: LiveData<OperationState<LiveData<PagedList<StockItem>>>>
        get() = _networkState

    init {
        if (transaction.transactionType != TransactionType.DISTRIBUTION &&
            transaction.distributedTo != null
        )
            throw UnsupportedOperationException(
                "Cannot set 'distributedTo' for non-distribution transactions"
            )

        if (transaction.transactionType == TransactionType.DISTRIBUTION &&
            transaction.distributedTo == null
        )
            throw UnsupportedOperationException("'distributedTo' is mandatory for model creation")

        speechRecognitionManager.supportNegativeNumberInput(
            transaction.transactionType == TransactionType.CORRECTION
        )

        configureRelays()
        loadStockItems()
    }

    private fun loadStockItems() {
        search.value = SearchParametersModel(null, null, transaction.facility.uid)
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
                            SearchParametersModel(result, null, transaction.facility.uid)
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
                                config.program,
                                transaction,
                                Date(),
                                config
                            )
                        )
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    fun onSearchQueryChanged(query: String) {
        searchRelay.accept(query)
    }

    fun onScanCompleted(itemCode: String) {
        search.postValue(SearchParametersModel(null, itemCode, transaction.facility.uid))
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

    fun getData(): ReviewStockData = ReviewStockData(transaction, getPopulatedEntries())

    fun getItemCount(): Int = itemsCache.size
}