package org.dhis2.android.rtsm.ui.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.commons.Constants.INTENT_EXTRA_APP_CONFIG
import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.android.rtsm.data.OperationState
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.data.models.Transaction
import org.dhis2.android.rtsm.exceptions.InitializationException
import org.dhis2.android.rtsm.exceptions.UserIntentParcelCreationException
import org.dhis2.android.rtsm.services.MetadataManager
import org.dhis2.android.rtsm.services.preferences.PreferenceProvider
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider
import org.dhis2.android.rtsm.ui.base.BaseViewModel
import org.dhis2.android.rtsm.utils.ParcelUtils
import org.dhis2.android.rtsm.utils.humanReadableDate
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val disposable: CompositeDisposable,
    private val schedulerProvider: BaseSchedulerProvider,
    preferenceProvider: PreferenceProvider,
    private val metadataManager: MetadataManager,
    savedState: SavedStateHandle
) : BaseViewModel(preferenceProvider, schedulerProvider) {

    val config: AppConfig = savedState.get<AppConfig>(INTENT_EXTRA_APP_CONFIG)
        ?: throw InitializationException("Some configuration parameters are missing")

    private val _transactionType = mutableStateOf(TransactionType.DISTRIBUTION)
    val transactionType: State<TransactionType> get() = _transactionType

    private val _isDistribution = mutableStateOf(false)
    val isDistribution: State<Boolean>
        get() = _isDistribution

    private val _facility = mutableStateOf<OrganisationUnit?>(null)
    val facility: State<OrganisationUnit?>
        get() = _facility

    private val _transactionDate = mutableStateOf(LocalDateTime.now())
    val transactionDate: State<LocalDateTime>
        get() = _transactionDate

    private val _destination = mutableStateOf<Option?>(null)
    val destination: State<Option?>
        get() = _destination

    private val _facilities = mutableStateOf<OperationState<List<OrganisationUnit>>>(OperationState.Loading)
    val facilities: State<OperationState<List<OrganisationUnit>>>
        get() = _facilities

    private val _destinations = mutableStateOf<OperationState<List<Option>>>(OperationState.Loading)
    val destinationsList: State<OperationState<List<Option>>>
        get() = _destinations

    // Toolbar section variables
    private val _toolbarTitle = mutableStateOf(TransactionType.DISTRIBUTION)
    val toolbarTitle: State<TransactionType> get() = _toolbarTitle

    private val _toolbarSubtitle = mutableStateOf("")
    val toolbarSubtitle: State<String> get() = _toolbarSubtitle

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
        return if (_transactionType.value == null) {
            R.string.mandatory_transaction_selection
        } else if (_facility.value == null) {
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
        if (transactionType.value == null) {
            throw UserIntentParcelCreationException(
                "Unable to create parcel with empty transaction type"
            )
        }

        if (facility.value == null) {
            throw UserIntentParcelCreationException(
                "Unable to create parcel with empty facility"
            )
        }

        if (transactionDate.value == null) {
            throw UserIntentParcelCreationException(
                "Unable to create parcel with empty transaction date"
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

    @JvmName("getToolbarTitle1")
    fun getToolbarTitle(): State<TransactionType> {
        return toolbarTitle
    }

    fun setToolbarTitle(transactionType: TransactionType) {
        _toolbarTitle.value = transactionType
    }

    fun fromFacilitiesLabel(from: String) {
        when(transactionType.value) {
            TransactionType.DISTRIBUTION -> _toolbarSubtitle.value = from
            TransactionType.DISCARD -> _toolbarSubtitle.value = from
            TransactionType.CORRECTION -> _toolbarSubtitle.value = from
        }
    }

    fun deliveryToLabel(to: String) {
        if (transactionType.value == TransactionType.DISTRIBUTION)
            _toolbarSubtitle.value = "${toolbarSubtitle.value} -> $to"
    }
}
