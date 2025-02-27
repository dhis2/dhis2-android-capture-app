package org.dhis2.android.rtsm.ui.home

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dhis2.org.analytics.charts.Charts
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.async
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
import javax.inject.Inject

@SuppressLint("MutableCollectionMutableState")
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    private val metadataManager: MetadataManager,
    private val charts: Charts,
    private val d2: D2,
    savedState: SavedStateHandle,
) : BaseViewModel(schedulerProvider) {

    private lateinit var config: StockUseCase

    private val program: String = savedState[Constants.PROGRAM_UID]
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

    private val _destinations =
        MutableStateFlow<OperationState<List<Option>>>(OperationState.Loading)
    val destinationsList: StateFlow<OperationState<List<Option>>>
        get() = _destinations

    private val _settingsUiSate = MutableStateFlow(SettingsUiState(programUid = program, transactionItems = transactionItems))
    val settingsUiState: StateFlow<SettingsUiState> = _settingsUiSate

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
            async {
                metadataManager.loadStockUseCase(programUid)
            }.await()?.let {
                config = it
            }
        }
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            val result = charts.getVisualizationGroups(program)
            if (result.isNotEmpty()) {
                _settingsUiSate.update { currentUiState ->
                    currentUiState.copy(hasAnalytics = result.isNotEmpty())
                }
                _analytics.value = result
            }
            val programName = d2.programModule().programs().uid(program).blockingGet()?.displayName()
            if (programName != null) {
                _settingsUiSate.update { currentUiState ->
                    currentUiState.copy(programName = programName)
                }
            }
        }
    }

    private fun loadDestinations() {
        disposable.add(
            metadataManager.destinations(config.distributedTo())
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { _destinations.value = (OperationState.Success<List<Option>>(it)) },
                    {
                        it.printStackTrace()
                        _destinations.value = (
                            OperationState.Error(R.string.destinations_load_error)
                            )
                    },
                ),
        )
    }

    private fun loadTransactionTypeLabels() {
        disposable.add(
            metadataManager.transactionType(config.stockDistribution())
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { dataElement ->
                        transactionItems.find { it.type == TransactionType.DISTRIBUTION }?.label = dataElement.displayName() ?: TransactionType.DISTRIBUTION.name
                        _settingsUiSate.update { currentUiState ->
                            currentUiState.copy(transactionItems = transactionItems, selectedTransactionItem = transactionItems.find { it.type == TransactionType.DISTRIBUTION } ?: currentUiState.selectedTransactionItem)
                        }
                    },
                    {
                        it.printStackTrace()
                    },
                ),
        )
        disposable.add(
            metadataManager.transactionType(config.stockCount())
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { dataElement ->
                        _helperText.value = dataElement.description()
                        transactionItems.find { it.type == TransactionType.CORRECTION }?.label = dataElement.displayName() ?: TransactionType.CORRECTION.name
                        _settingsUiSate.update { currentUiState ->
                            currentUiState.copy(transactionItems = transactionItems)
                        }
                    },
                    {
                        it.printStackTrace()
                    },
                ),
        )
        disposable.add(
            metadataManager.transactionType(config.stockDiscarded())
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { dataElement ->
                        transactionItems.find { it.type == TransactionType.DISCARD }?.label = dataElement.displayName() ?: TransactionType.DISCARD.name
                        _settingsUiSate.update { currentUiState ->
                            currentUiState.copy(transactionItems = transactionItems)
                        }
                    },
                    {
                        it.printStackTrace()
                    },
                ),
        )
        disposable.add(
            metadataManager.transactionType(config.distributedTo())
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        _settingsUiSate.update { currentUiState ->
                            currentUiState.copy(deliverToLabel = it.displayName() ?: "")
                        }
                    },
                    {
                        it.printStackTrace()
                    },
                ),
        )
    }

    private fun loadFacilities() {
        disposable.add(
            metadataManager.facilities(config.programUid)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        _facilities.value = (OperationState.Success(it))

                        if (it.size == 1) {
                            _settingsUiSate.update { currentUiState ->
                                currentUiState.copy(facility = it[0])
                            }
                        }
                    },
                    {
                        it.printStackTrace()
                        _facilities.value = (OperationState.Error(R.string.facilities_load_error))
                    },
                ),
        )
    }

    fun selectTransaction(selectedItem: TransactionItem) {
        _settingsUiSate.update { currentUiState ->
            currentUiState.copy(selectedTransactionItem = selectedItem)
        }
        // Distributed to cannot only be set for DISTRIBUTION,
        // so ensure you clear it for others if it has been set
        if (selectedItem.type != TransactionType.DISTRIBUTION) {
            _settingsUiSate.update { currentUiState ->
                currentUiState.copy(destination = null)
            }
        }
    }

    fun setFacility(facility: OrganisationUnit) {
        _settingsUiSate.update { currentUiState ->
            currentUiState.copy(facility = facility)
        }
    }

    fun setDestination(destination: Option?) {
        if (settingsUiState.value.selectedTransactionItem.type != TransactionType.DISTRIBUTION) {
            throw UnsupportedOperationException(
                "Cannot set 'distributed to' for non-distribution transactions",
            )
        }

        _settingsUiSate.update { currentUiState ->
            currentUiState.copy(destination = destination)
        }
    }

    fun checkForFieldErrors(): Int? {
        return if (settingsUiState.value.facility == null) {
            R.string.mandatory_facility_selection
        } else if (settingsUiState.value.selectedTransactionItem.type == TransactionType.DISTRIBUTION &&
            settingsUiState.value.destination == null
        ) {
            R.string.mandatory_distributed_to_selection
        } else {
            null
        }
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
        _settingsUiSate.update {
            SettingsUiState(
                programUid = config.programUid,
                transactionItems = transactionItems,
            )
        }
        selectTransaction(
            transactionItems.find { it.type == TransactionType.DISTRIBUTION } ?: TransactionItem(
                R.drawable.ic_distribution,
                TransactionType.DISTRIBUTION, TransactionType.DISTRIBUTION.name,
            ),
        )
    }

    private fun mapTransaction(): MutableList<TransactionItem> {
        return mutableListOf(
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
    }

    fun switchScreen(itemId: Int) {
        when (itemId) {
            BottomNavigation.DATA_ENTRY.id -> {
                _settingsUiSate.update { currentUiState ->
                    currentUiState.copy(selectedScreen = BottomNavigation.DATA_ENTRY)
                }
            }
            BottomNavigation.ANALYTICS.id -> {
                _settingsUiSate.update { currentUiState ->
                    currentUiState.copy(selectedScreen = BottomNavigation.ANALYTICS)
                }
            }
        }
    }
}
