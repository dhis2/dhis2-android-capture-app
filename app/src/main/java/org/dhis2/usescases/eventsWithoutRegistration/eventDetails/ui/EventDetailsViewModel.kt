package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.dhis2.commons.extensions.truncate
import org.dhis2.commons.locationprovider.LocationProvider
import org.dhis2.form.data.GeometryController
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventCatCombo
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventCoordinates
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventReportDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventTemp
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureOrgUnit
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.CreateOrUpdateEventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCatCombo
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCoordinates
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventOrgUnit
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventTemp
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventTempStatus
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.DEFAULT_MAX_DATE
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.DEFAULT_MIN_DATE
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.EventDetailResourcesProvider
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

class EventDetailsViewModel(
    private val configureEventDetails: ConfigureEventDetails,
    private val configureEventReportDate: ConfigureEventReportDate,
    private val configureOrgUnit: ConfigureOrgUnit,
    private val configureEventCoordinates: ConfigureEventCoordinates,
    private val configureEventCatCombo: ConfigureEventCatCombo,
    private val configureEventTemp: ConfigureEventTemp,
    private val periodType: PeriodType?,
    private val eventUid: String?,
    private val geometryController: GeometryController,
    private val locationProvider: LocationProvider,
    private val createOrUpdateEventDetails: CreateOrUpdateEventDetails,
    private val resourcesProvider: EventDetailResourcesProvider,
) : ViewModel() {

    var showPeriods: (() -> Unit)? = null
    var showOrgUnits: (() -> Unit)? = null
    var showNoOrgUnits: (() -> Unit)? = null
    var requestLocationPermissions: (() -> Unit)? = null
    var showEnableLocationMessage: (() -> Unit)? = null
    var requestLocationByMap: ((featureType: String, initCoordinate: String?) -> Unit)? = null
    var onButtonClickCallback: (() -> Unit)? = null
    var showEventUpdateStatus: ((result: String) -> Unit)? = null
    var onReopenError: ((message: String) -> Unit)? = null
    var onReopenSuccess: ((message: String) -> Unit)? = null

    private val _eventDetails: MutableStateFlow<EventDetails> = MutableStateFlow(EventDetails())
    val eventDetails: StateFlow<EventDetails> get() = _eventDetails

    private val _eventDate: MutableStateFlow<EventDate> = MutableStateFlow(EventDate())
    val eventDate: StateFlow<EventDate> get() = _eventDate

    private val _eventOrgUnit: MutableStateFlow<EventOrgUnit> = MutableStateFlow(EventOrgUnit())
    val eventOrgUnit: StateFlow<EventOrgUnit> get() = _eventOrgUnit

    private val _eventCoordinates: MutableStateFlow<EventCoordinates> =
        MutableStateFlow(EventCoordinates())
    val eventCoordinates: StateFlow<EventCoordinates> get() = _eventCoordinates

    private val _eventCatCombo: MutableStateFlow<EventCatCombo> = MutableStateFlow(EventCatCombo())
    val eventCatCombo: StateFlow<EventCatCombo> get() = _eventCatCombo

    private val _eventTemp: MutableStateFlow<EventTemp> = MutableStateFlow(EventTemp())
    val eventTemp: StateFlow<EventTemp> get() = _eventTemp

    init {
        loadEventDetails()
    }

    private fun loadEventDetails() {
        viewModelScope.launch {
            configureEventReportDate().collect {
                _eventDate.value = it
            }

            configureOrgUnit(eventDate.value.currentDate)
                .collect {
                    _eventOrgUnit.value = it
                }

            configureEventCatCombo()
                .collect {
                    _eventCatCombo.value = it
                }

            configureEventCoordinates("")
                .collect { eventCoordinates ->
                    eventCoordinates.model?.setCallback(
                        geometryController.getCoordinatesCallback(
                            updateCoordinates = { value ->
                                setUpCoordinates(value)
                            },
                            currentLocation = {
                                requestCurrentLocation()
                            },
                            mapRequest = { _, featureType, initCoordinate ->
                                requestLocationByMap?.invoke(featureType, initCoordinate)
                            },
                        ),
                    )
                    _eventCoordinates.value = eventCoordinates
                }

            configureEventTemp().apply {
                _eventTemp.value = this
            }

            configureEventDetails(
                selectedDate = eventDate.value.currentDate,
                selectedOrgUnit = eventOrgUnit.value.selectedOrgUnit?.uid(),
                catOptionComboUid = eventCatCombo.value.uid,
                isCatComboCompleted = eventCatCombo.value.isCompleted,
                coordinates = eventCoordinates.value.model?.value,
                tempCreate = eventTemp.value.status?.name,
            )
                .collect {
                    _eventDetails.value = it
                }
        }
    }

    private fun setUpEventDetails() {
        EventDetailIdlingResourceSingleton.increment()
        viewModelScope.launch {
            configureEventDetails(
                selectedDate = eventDate.value.currentDate,
                selectedOrgUnit = eventOrgUnit.value.selectedOrgUnit?.uid(),
                catOptionComboUid = eventCatCombo.value.uid,
                isCatComboCompleted = eventCatCombo.value.isCompleted,
                coordinates = eventCoordinates.value.model?.value,
                tempCreate = eventTemp.value.status?.name,
            )
                .flowOn(Dispatchers.IO)
                .collect {
                    _eventDetails.value = it
                    EventDetailIdlingResourceSingleton.decrement()
                }
        }
    }

    fun setUpEventReportDate(selectedDate: Date? = null) {
        EventDetailIdlingResourceSingleton.increment()
        viewModelScope.launch {
            configureEventReportDate(selectedDate)
                .flowOn(Dispatchers.IO)
                .collect {
                    _eventDate.value = it
                    setUpEventDetails()
                    setUpOrgUnit(selectedDate = it.currentDate)
                    EventDetailIdlingResourceSingleton.decrement()
                }
        }
    }

    fun onClearEventReportDate() {
        _eventDate.value = eventDate.value.copy(currentDate = null, dateValue = null)
        setUpEventDetails()
    }

    fun setUpOrgUnit(selectedDate: Date? = null, selectedOrgUnit: String? = null) {
        viewModelScope.launch {
            configureOrgUnit(selectedDate, selectedOrgUnit)
                .flowOn(Dispatchers.IO)
                .collect {
                    _eventOrgUnit.value = it
                    setUpEventDetails()
                }
        }
    }

    fun onClearOrgUnit() {
        _eventOrgUnit.value = eventOrgUnit.value.copy(selectedOrgUnit = null)
        setUpEventDetails()
    }

    fun setUpCategoryCombo(categoryOption: Pair<String, String?>? = null) {
        EventDetailIdlingResourceSingleton.increment()
        viewModelScope.launch {
            configureEventCatCombo(categoryOption)
                .flowOn(Dispatchers.IO)
                .collect {
                    _eventCatCombo.value = it
                    setUpEventDetails()
                    EventDetailIdlingResourceSingleton.decrement()
                }
        }
    }

    fun onClearCatCombo() {
        _eventCatCombo.value = eventCatCombo.value.copy(isCompleted = false)
        setUpEventDetails()
    }

    private fun setUpCoordinates(value: String? = "") {
        EventDetailIdlingResourceSingleton.increment()
        viewModelScope.launch {
            configureEventCoordinates(value)
                .flowOn(Dispatchers.IO)
                .collect { eventCoordinates ->
                    eventCoordinates.model?.setCallback(
                        geometryController.getCoordinatesCallback(
                            updateCoordinates = { value ->
                                setUpCoordinates(value)
                            },
                            currentLocation = {
                                requestCurrentLocation()
                            },
                            mapRequest = { _, featureType, initCoordinate ->
                                requestLocationByMap?.invoke(featureType, initCoordinate)
                            },
                        ),
                    )
                    _eventCoordinates.value = eventCoordinates
                    setUpEventDetails()
                    EventDetailIdlingResourceSingleton.decrement()
                }
        }
    }

    fun setUpEventTemp(status: EventTempStatus? = null, isChecked: Boolean = true) {
        EventDetailIdlingResourceSingleton.increment()
        if (isChecked) {
            configureEventTemp(status).apply {
                _eventTemp.value = this
                setUpEventDetails()
            }
        }
        EventDetailIdlingResourceSingleton.decrement()
    }

    fun getSelectableDates(eventDate: EventDate): SelectableDates {
        return if (eventDate.allowFutureDates) {
            SelectableDates(DEFAULT_MIN_DATE, DEFAULT_MAX_DATE)
        } else {
            val currentDate = SimpleDateFormat("ddMMyyyy", Locale.US).format(Date(System.currentTimeMillis()))
            SelectableDates(DEFAULT_MIN_DATE, currentDate)
        }
    }

    fun showPeriodDialog() {
        periodType?.let {
            showPeriods?.invoke()
        }
    }

    fun onDateSet(year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance()
        calendar[year, month, day, 0, 0] = 0
        calendar[Calendar.MILLISECOND] = 0

        val currentTimeZone: TimeZone = calendar.getTimeZone()
        val currentDt: Calendar = GregorianCalendar(currentTimeZone, Locale.getDefault())

        var gmtOffset: Int = currentTimeZone.getOffset(
            currentDt[Calendar.ERA],
            currentDt[Calendar.YEAR],
            currentDt[Calendar.MONTH],
            currentDt[Calendar.DAY_OF_MONTH],
            currentDt[Calendar.DAY_OF_WEEK],
            currentDt[Calendar.MILLISECOND],
        )
        gmtOffset /= (60 * 60 * 1000)
        calendar.add(Calendar.HOUR_OF_DAY, +gmtOffset)
        val selectedDate = calendar.time

        setUpEventReportDate(selectedDate)
    }

    fun onOrgUnitClick() {
        if (!eventOrgUnit.value.fixed) {
            if (eventOrgUnit.value.orgUnits.isNullOrEmpty()) {
                showNoOrgUnits?.invoke()
            } else {
                showOrgUnits?.invoke()
            }
        }
    }

    fun requestCurrentLocation() {
        locationProvider.getLastKnownLocation(
            onNewLocation = { location ->
                val longitude = location.longitude.truncate()
                val latitude = location.latitude.truncate()
                val geometry =
                    GeometryHelper.createPointGeometry(longitude, latitude)
                setUpCoordinates(geometry.coordinates())
            },
            onPermissionNeeded = {
                requestLocationPermissions?.invoke()
            },
            onLocationDisabled = {
                showEnableLocationMessage?.invoke()
            },
        )
    }

    fun onLocationByMapSelected(featureType: FeatureType, coordinates: String?) {
        val geometry: Geometry? = geometryController.generateLocationFromCoordinates(
            featureType,
            coordinates,
        )
        geometry?.let { setUpCoordinates(it.coordinates()) }
    }

    fun onButtonClick() {
        if (eventUid != null) {
            updateEventDetails()
        }
        onButtonClickCallback?.invoke()
    }

    fun updateEventDetails() {
        viewModelScope.launch {
            eventDetails.value.apply {
                selectedDate?.let { date ->
                    createOrUpdateEventDetails(
                        selectedDate = date,
                        selectedOrgUnit = selectedOrgUnit,
                        catOptionComboUid = catOptionComboUid,
                        coordinates = coordinates,
                    ).flowOn(Dispatchers.IO)
                        .collect { result ->
                            result.onFailure {
                                showEventUpdateStatus?.invoke(it.message!!)
                            }
                            result.onSuccess { message ->
                                showEventUpdateStatus?.invoke(message)
                            }
                        }
                }
            }
        }
    }

    fun getPeriodType(): PeriodType? {
        return periodType
    }

    fun onReopenClick() {
        configureEventDetails.reopenEvent().mockSafeFold(
            onSuccess = {
                loadEventDetails()
                onReopenSuccess?.invoke(resourcesProvider.provideReOpened())
            },
            onFailure = { error -> error.message?.let { onReopenError?.invoke(it) } },
        )
    }

    fun cancelCoordinateRequest() {
        setUpCoordinates(value = eventCoordinates.value.model?.value)
    }
}

inline fun <R, reified T> Result<T>.mockSafeFold(
    onSuccess: (value: T) -> R,
    onFailure: (exception: Throwable) -> R,
): R = when {
    isSuccess -> {
        val value = getOrNull()
        try {
            onSuccess(value as T)
        } catch (e: ClassCastException) {
            // This block of code is only executed in testing environment, when we are mocking a
            // function that returns a `Result` object.
            val valueNotNull = value!!
            if ((value as Result<*>).isSuccess) {
                valueNotNull::class.java.getDeclaredField("value").let {
                    it.isAccessible = true
                    it.get(value) as T
                }.let(onSuccess)
            } else {
                valueNotNull::class.java.getDeclaredField("value").let {
                    it.isAccessible = true
                    it.get(value)
                }.let { failure ->
                    failure!!::class.java.getDeclaredField("exception").let {
                        it.isAccessible = true
                        it.get(failure) as Exception
                    }
                }.let(onFailure)
            }
        }
    }

    else -> onFailure(exceptionOrNull() ?: Exception())
}
