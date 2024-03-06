package org.dhis2.android.rtsm.ui.home

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.commons.Constants.INTENT_EXTRA_APP_CONFIG
import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.android.rtsm.data.OperationState
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.data.models.Transaction
import org.dhis2.android.rtsm.exceptions.InitializationException
import org.dhis2.android.rtsm.exceptions.UserIntentParcelCreationException
import org.dhis2.android.rtsm.services.MetadataManager
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider
import org.dhis2.android.rtsm.ui.base.BaseViewModel
import org.dhis2.android.rtsm.ui.home.model.SettingsUiState
import org.dhis2.android.rtsm.utils.ParcelUtils
import org.dhis2.android.rtsm.utils.humanReadableDate
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    private val metadataManager: MetadataManager,
    savedState: SavedStateHandle
) : BaseViewModel(schedulerProvider) {

    private val config: AppConfig = savedState.get<AppConfig>(INTENT_EXTRA_APP_CONFIG)
        ?: throw InitializationException("Some configuration parameters are missing")

    private val _facilities =
        MutableStateFlow<OperationState<List<OrganisationUnit>>>(OperationState.Loading)
    val facilities: StateFlow<OperationState<List<OrganisationUnit>>>
        get() = _facilities

    private val _destinations =
        MutableStateFlow<OperationState<List<Option>>>(OperationState.Loading)
    val destinationsList: StateFlow<OperationState<List<Option>>>
        get() = _destinations

    private val _settingsUiSate = MutableStateFlow(SettingsUiState(programUid = config.program))
    val settingsUiState: StateFlow<SettingsUiState> = _settingsUiSate

    init {
        loadFacilities()
        loadDestinations()
    }

    private fun loadDestinations() {
        disposable.add(
            metadataManager.destinations(config.distributedTo)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { _destinations.value = (OperationState.Success<List<Option>>(it)) },
                    {
                        it.printStackTrace()
                        _destinations.value = (
                            OperationState.Error(R.string.destinations_load_error)
                            )
                    }
                )
        )
    }

    private fun loadFacilities() {
        disposable.add(
            metadataManager.facilities(config.program)
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
                    }
                )
        )
    }

    fun selectTransaction(type: TransactionType) {
        _settingsUiSate.update { currentUiState ->
            currentUiState.copy(transactionType = type)
        }

        // Distributed to cannot only be set for DISTRIBUTION,
        // so ensure you clear it for others if it has been set
        if (type != TransactionType.DISTRIBUTION) {
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
        if (settingsUiState.value.transactionType != TransactionType.DISTRIBUTION) {
            throw UnsupportedOperationException(
                "Cannot set 'distributed to' for non-distribution transactions"
            )
        }

        _settingsUiSate.update { currentUiState ->
            currentUiState.copy(destination = destination)
        }
    }

    fun checkForFieldErrors(): Int? {
        return if (settingsUiState.value.facility == null) {
            R.string.mandatory_facility_selection
        } else if (settingsUiState.value.transactionType == TransactionType.DISTRIBUTION &&
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
                "Unable to create parcel with empty facility"
            )
        }
        return Transaction(
            settingsUiState.value.transactionType,
            ParcelUtils.facilityToIdentifiableModelParcel(settingsUiState.value.facility!!),
            settingsUiState.value.transactionDate.humanReadableDate(),
            settingsUiState.value.destination?.let {
                ParcelUtils.distributedTo_ToIdentifiableModelParcel(
                    it
                )
            }
        )
    }

    fun resetSettings() {
        _settingsUiSate.update {
            SettingsUiState(
                programUid = config.program
            )
        }
        selectTransaction(TransactionType.DISTRIBUTION)
    }
}
