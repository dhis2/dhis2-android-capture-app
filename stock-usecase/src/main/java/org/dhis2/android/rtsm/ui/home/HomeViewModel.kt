package org.dhis2.android.rtsm.ui.home

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dhis2.org.analytics.charts.Charts
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.data.OperationState
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.data.models.Transaction
import org.dhis2.android.rtsm.data.models.TransactionItem
import org.dhis2.android.rtsm.exceptions.InitializationException
import org.dhis2.android.rtsm.exceptions.UserIntentParcelCreationException
import org.dhis2.android.rtsm.services.MetadataManager
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider
import org.dhis2.android.rtsm.ui.base.BaseViewModel
import org.dhis2.android.rtsm.ui.home.model.SettingsUiState
import org.dhis2.android.rtsm.ui.home.screens.BottomNavigation
import org.dhis2.android.rtsm.utils.ParcelUtils
import org.dhis2.android.rtsm.utils.humanReadableDate
import org.dhis2.commons.Constants
import org.dhis2.commons.bindings.distributedTo
import org.dhis2.commons.bindings.stockCount
import org.dhis2.commons.bindings.stockDiscarded
import org.dhis2.commons.bindings.stockDistribution
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.settings.AnalyticsDhisVisualizationsGroup
import org.hisp.dhis.android.core.usecase.stock.StockUseCase
import timber.log.Timber

@SuppressLint("MutableCollectionMutableState")
class HomeViewModel(
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    private val metadataManager: MetadataManager,
    private val charts: Charts,
    private val d2: D2,
    savedState: SavedStateHandle,
) : BaseViewModel(schedulerProvider) {
    private lateinit var config: StockUseCase

    private val program: String =
        savedState[Constants.PROGRAM_UID]
            ?: throw InitializationException("Some configuration parameters are missing")

    private val _facilities =
        MutableStateFlow<OperationState<List<OrganisationUnit>>>(OperationState.Loading)
    val facilities: StateFlow<OperationState<List<OrganisationUnit>>>
        get() = _facilities

    private val _analytics =
        MutableStateFlow<List<AnalyticsDhisVisualizationsGroup>>(emptyList())
    val analytics: StateFlow<List<AnalyticsDhisVisualizationsGroup>>
        get() = _analytics

    private var transactionItems by mutableStateOf(mapTransaction())

    private val _destinationsList =
        MutableStateFlow<OperationState<List<Option>>>(OperationState.Loading)
    val destinationsList: StateFlow<OperationState<List<Option>>>
        get() = _destinationsList

    private val _settingsUiState = MutableStateFlow(SettingsUiState(programUid = program, transactionItems = transactionItems))
    val settingsUiState: StateFlow<SettingsUiState> = _settingsUiState

    private val _helperText = MutableStateFlow<String?>(null)
    val helperText = _helperText.asStateFlow()

    init {
        loadStockUseCases(program)
        loadAnalytics()
        loadFacilities()
        loadDestinations()
        loadTransactionTypeLabels()
    }

    private fun loadStockUseCases(programUid: String) {
        viewModelScope.launch {
            metadataManager.loadStockUseCase(programUid)?.let {
                config = it
            }
        }
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            val result = charts.getVisualizationGroups(program)
            if (result.isNotEmpty()) {
                _settingsUiState.update { currentUiState ->
                    currentUiState.copy(hasAnalytics = result.isNotEmpty())
                }
                _analytics.value = result
            }
            val programName =
                d2
                    .programModule()
                    .programs()
                    .uid(program)
                    .blockingGet()
                    ?.displayName()
            if (programName != null) {
                _settingsUiState.update { currentUiState ->
                    currentUiState.copy(programName = programName)
                }
            }
        }
    }

    private fun loadDestinations() {
        disposable.add(
            metadataManager
                .destinations(config.distributedTo())
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { _destinationsList.value = (OperationState.Success<List<Option>>(it)) },
                    {
                        Timber.e(it)
                        _destinationsList.value = (
                            OperationState.Error(R.string.destinations_load_error)
                        )
                    },
                ),
        )
    }

    private fun loadTransactionTypeLabels() {
        disposable.add(
            metadataManager
                .transactionType(config.stockDistribution())
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { dataElement ->
                        transactionItems.find { it.type == TransactionType.DISTRIBUTION }?.label =
                            dataElement.displayName() ?: TransactionType.DISTRIBUTION.name
                        _settingsUiState.update { currentUiState ->
                            currentUiState.copy(
                                transactionItems = transactionItems,
                                selectedTransactionItem =
                                    transactionItems.find { it.type == TransactionType.DISTRIBUTION }
                                        ?: currentUiState.selectedTransactionItem,
                            )
                        }
                    },
                    {
                        Timber.e(it)
                    },
                ),
        )
        disposable.add(
            metadataManager
                .transactionType(config.stockCount())
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { dataElement ->
                        _helperText.value = dataElement.description()
                        transactionItems.find { it.type == TransactionType.CORRECTION }?.label =
                            dataElement.displayName() ?: TransactionType.CORRECTION.name
                        _settingsUiState.update { currentUiState ->
                            currentUiState.copy(transactionItems = transactionItems)
                        }
                    },
                    {
                        Timber.e(it)
                    },
                ),
        )
        disposable.add(
            metadataManager
                .transactionType(config.stockDiscarded())
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { dataElement ->
                        transactionItems.find { it.type == TransactionType.DISCARD }?.label =
                            dataElement.displayName() ?: TransactionType.DISCARD.name
                        _settingsUiState.update { currentUiState ->
                            currentUiState.copy(transactionItems = transactionItems)
                        }
                    },
                    {
                        Timber.e(it)
                    },
                ),
        )
        disposable.add(
            metadataManager
                .transactionType(config.distributedTo())
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        _settingsUiState.update { currentUiState ->
                            currentUiState.copy(deliverToLabel = it.displayName() ?: "")
                        }
                    },
                    {
                        Timber.e(it)
                    },
                ),
        )
    }

    private fun loadFacilities() {
        disposable.add(
            metadataManager
                .facilities(config.programUid)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        _facilities.value = (OperationState.Success(it))

                        if (it.size == 1) {
                            _settingsUiState.update { currentUiState ->
                                currentUiState.copy(facility = it[0])
                            }
                        }
                    },
                    {
                        Timber.e(it)
                        _facilities.value = (OperationState.Error(R.string.facilities_load_error))
                    },
                ),
        )
    }

    fun selectTransaction(selectedItem: TransactionItem) {
        _settingsUiState.update { currentUiState ->
            currentUiState.copy(selectedTransactionItem = selectedItem)
        }
        // Distributed to cannot only be set for DISTRIBUTION,
        // so ensure you clear it for others if it has been set
        if (selectedItem.type != TransactionType.DISTRIBUTION) {
            _settingsUiState.update { currentUiState ->
                currentUiState.copy(destination = null)
            }
        }
    }

    fun setFacility(facility: OrganisationUnit) {
        _settingsUiState.update { currentUiState ->
            currentUiState.copy(facility = facility)
        }
    }

    fun setDestination(destination: Option?) {
        if (settingsUiState.value.selectedTransactionItem.type != TransactionType.DISTRIBUTION) {
            throw UnsupportedOperationException(
                "Cannot set 'distributed to' for non-distribution transactions",
            )
        }

        _settingsUiState.update { currentUiState ->
            currentUiState.copy(destination = destination)
        }
    }

    fun checkForFieldErrors(): Int? =
        if (settingsUiState.value.facility == null) {
            R.string.mandatory_facility_selection
        } else if (settingsUiState.value.selectedTransactionItem.type == TransactionType.DISTRIBUTION &&
            settingsUiState.value.destination == null
        ) {
            R.string.mandatory_distributed_to_selection
        } else {
            null
        }

    fun getData(): Transaction {
        if (settingsUiState.value.facility == null) {
            throw UserIntentParcelCreationException(
                "Unable to create parcel with empty facility",
            )
        }
        return Transaction(
            settingsUiState.value.selectedTransactionItem.type,
            ParcelUtils.facilityToIdentifiableModelParcel(settingsUiState.value.facility!!),
            settingsUiState.value.transactionDate.humanReadableDate(),
            settingsUiState.value.destination?.let {
                ParcelUtils.distributedTo_ToIdentifiableModelParcel(
                    it,
                )
            },
        )
    }

    fun resetSettings() {
        _settingsUiState.update {
            SettingsUiState(
                programUid = config.programUid,
                transactionItems = transactionItems,
            )
        }
        selectTransaction(
            transactionItems.find { it.type == TransactionType.DISTRIBUTION } ?: TransactionItem(
                R.drawable.ic_distribution,
                TransactionType.DISTRIBUTION,
                TransactionType.DISTRIBUTION.name,
            ),
        )
    }

    private fun mapTransaction(): MutableList<TransactionItem> =
        mutableListOf(
            TransactionItem(
                R.drawable.ic_distribution,
                TransactionType.DISTRIBUTION,
                TransactionType.DISTRIBUTION.name,
            ),
            TransactionItem(R.drawable.ic_discard, TransactionType.DISCARD, TransactionType.DISCARD.name),
            TransactionItem(
                R.drawable.ic_correction,
                TransactionType.CORRECTION,
                TransactionType.CORRECTION.name,
            ),
        )

    fun switchScreen(itemId: Int) {
        when (itemId) {
            BottomNavigation.DATA_ENTRY.id -> {
                _settingsUiState.update { currentUiState ->
                    currentUiState.copy(selectedScreen = BottomNavigation.DATA_ENTRY)
                }
            }
            BottomNavigation.ANALYTICS.id -> {
                _settingsUiState.update { currentUiState ->
                    currentUiState.copy(selectedScreen = BottomNavigation.ANALYTICS)
                }
            }
        }
    }
}
