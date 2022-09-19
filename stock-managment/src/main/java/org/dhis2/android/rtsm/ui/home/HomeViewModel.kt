package org.dhis2.android.rtsm.ui.home

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.commons.Constants.INSTANT_DATA_SYNC
import org.dhis2.android.rtsm.commons.Constants.INTENT_EXTRA_APP_CONFIG
import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.android.rtsm.data.OperationState
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.data.models.Transaction
import org.dhis2.android.rtsm.exceptions.InitializationException
import org.dhis2.android.rtsm.exceptions.UserIntentParcelCreationException
import org.dhis2.android.rtsm.services.MetadataManager
import org.dhis2.android.rtsm.services.SyncManager
import org.dhis2.android.rtsm.services.preferences.PreferenceProvider
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider
import org.dhis2.android.rtsm.ui.base.BaseViewModel
import org.dhis2.android.rtsm.utils.ParcelUtils
import org.dhis2.android.rtsm.utils.humanReadableDate
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    preferenceProvider: PreferenceProvider,
    private val metadataManager: MetadataManager,
    private val syncManager: SyncManager,
    savedState: SavedStateHandle
) : BaseViewModel(preferenceProvider, schedulerProvider) {

    val config: AppConfig = savedState.get<AppConfig>(INTENT_EXTRA_APP_CONFIG)
        ?: throw InitializationException("Some configuration parameters are missing")

    private val _transactionType = MutableStateFlow(TransactionType.DISTRIBUTION)
    val transactionType: StateFlow<TransactionType> get() = _transactionType

    private val _isDistribution = MutableStateFlow(true)
    val isDistribution: StateFlow<Boolean>
        get() = _isDistribution

    private val _facility = MutableStateFlow<OrganisationUnit?>(null)
    val facility: StateFlow<OrganisationUnit?>
        get() = _facility

    private val _transactionDate = MutableStateFlow(LocalDateTime.now())
    val transactionDate: StateFlow<LocalDateTime>
        get() = _transactionDate

    private val _destination = MutableStateFlow<Option?>(null)
    val destination: StateFlow<Option?>
        get() = _destination

    private val _facilities =
        MutableStateFlow<OperationState<List<OrganisationUnit>>>(OperationState.Loading)
    val facilities: StateFlow<OperationState<List<OrganisationUnit>>>
        get() = _facilities

    private val _destinations =
        MutableStateFlow<OperationState<List<Option>>>(OperationState.Loading)
    val destinationsList: StateFlow<OperationState<List<Option>>>
        get() = _destinations

    // Toolbar section variables
    private val _toolbarTitle = MutableStateFlow(TransactionType.DISTRIBUTION)
    val toolbarTitle: StateFlow<TransactionType> get() = _toolbarTitle

    private val _fromFacility = MutableStateFlow("From...")
    val fromFacility: StateFlow<String> get() = _fromFacility

    private val _deliveryTo = MutableStateFlow<String?>(null)
    val deliveryTo: StateFlow<String?> get() = _deliveryTo

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

                        if (it.size == 1) _facility.value = (it[0])
                    },
                    {
                        it.printStackTrace()
                        _facilities.value = (OperationState.Error(R.string.facilities_load_error))
                    }
                )
        )
    }

    fun selectTransaction(type: TransactionType) {
        _transactionType.value = type
        _isDistribution.value = type == TransactionType.DISTRIBUTION

        // Distributed to cannot only be set for DISTRIBUTION,
        // so ensure you clear it for others if it has been set
        if (type != TransactionType.DISTRIBUTION) {
            _destination.value = null
            _deliveryTo.value = null
        }
    }

    fun setFacility(facility: OrganisationUnit) {
        _facility.value = facility
    }

    fun setDestination(destination: Option?) {
        if (!isDistribution.value) {
            throw UnsupportedOperationException(
                "Cannot set 'distributed to' for non-distribution transactions"
            )
        }

        _destination.value = destination
    }

    fun checkForFieldErrors(): Int? {
        return if (_facility.value == null) {
            R.string.mandatory_facility_selection
        } else if (_transactionDate.value == null) {
            R.string.mandatory_transaction_date_selection
        } else if (_transactionType.value == TransactionType.DISTRIBUTION &&
            _destination.value == null
        ) {
            R.string.mandatory_distributed_to_selection
        } else {
            null
        }
    }

    fun getData(): Transaction {
        if (facility.value == null) {
            throw UserIntentParcelCreationException(
                "Unable to create parcel with empty facility"
            )
        }

        return Transaction(
            transactionType.value,
            ParcelUtils.facilityToIdentifiableModelParcel(facility.value!!),
            transactionDate.value.humanReadableDate(),
            destination.value?.let { ParcelUtils.distributedTo_ToIdentifiableModelParcel(it) }
        )
    }

    fun setTransactionDate(epoch: Long) {
        _transactionDate.value = Instant.ofEpochMilli(epoch)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }

    fun setToolbarTitle(transactionType: TransactionType) {
        _toolbarTitle.value = transactionType
    }

    fun fromFacilitiesLabel(from: String) {
        when (transactionType.value) {
            TransactionType.DISTRIBUTION -> _fromFacility.value = "From $from"
            TransactionType.DISCARD -> _fromFacility.value = "From $from"
            TransactionType.CORRECTION -> _fromFacility.value = "From $from"
        }
    }

    fun deliveryToLabel(to: String) {
        if (transactionType.value == TransactionType.DISTRIBUTION) {
            _deliveryTo.value = "To $to"
        }
    }

    fun syncData() {
        syncManager.dataSync()
    }
    fun getSyncDataStatus() = syncManager.getSyncStatus(INSTANT_DATA_SYNC)
}
