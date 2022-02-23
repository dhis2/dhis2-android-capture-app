package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.dhis2.commons.extensions.truncate
import org.dhis2.data.location.LocationProvider
import org.dhis2.form.data.GeometryController
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventCatCombo
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventCoordinates
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventReportDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventTemp
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureOrgUnit
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCatCombo
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCategory
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCoordinates
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventOrgUnit
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventTemp
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventTempStatus
import org.dhis2.utils.category.CategoryDialog.Companion.DEFAULT_COUNT_LIMIT
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.period.PeriodType
import java.util.Calendar
import java.util.Date

class EventDetailsViewModel(
    private val configureEventDetails: ConfigureEventDetails,
    private val configureEventReportDate: ConfigureEventReportDate,
    private val configureOrgUnit: ConfigureOrgUnit,
    private val configureEventCoordinates: ConfigureEventCoordinates,
    private val configureEventCatCombo: ConfigureEventCatCombo,
    private val configureEventTemp: ConfigureEventTemp,
    private val periodType: PeriodType?,
    private val geometryController: GeometryController,
    private val locationProvider: LocationProvider
) : ViewModel() {

    var showCalendar: (() -> Unit)? = null
    var showPeriods: (() -> Unit)? = null
    var showOrgUnits: (() -> Unit)? = null
    var showCategoryDialog: ((category: EventCategory) -> Unit)? = null
    var showCategoryPopUp: ((category: EventCategory) -> Unit)? = null
    var requestLocationPermissions: (() -> Unit)? = null
    var showEnableLocationMessage: (() -> Unit)? = null
    var requestLocationByMap: ((featureType: String, initCoordinate: String?) -> Unit)? = null

    private val _eventDetails: MutableStateFlow<EventDetails?> = MutableStateFlow(null)
    val eventDetails: StateFlow<EventDetails?> get() = _eventDetails

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
        setUpEventDetails()
        setUpEventReportDate()
        setUpCategoryCombo()
        setUpCoordinates()
        setupEventTemp()
    }

    private fun setUpEventDetails() {
        viewModelScope.launch {
            _eventDetails.value = configureEventDetails()
        }
    }

    private fun setUpEventReportDate() {
        viewModelScope.launch {
            _eventDate.value = configureEventReportDate()
            eventDate.collect {
                setUpOrgUnit(it.currentDate)
            }
        }
    }

    private fun setUpOrgUnit(currentDate: Date?) {
        viewModelScope.launch {
            _eventOrgUnit.value = configureOrgUnit(currentDate)
        }
    }

    private fun setUpCategoryCombo() {
        viewModelScope.launch {
            _eventCatCombo.value = configureEventCatCombo()
        }
    }

    private fun setUpCoordinates(value: String? = null) {
        viewModelScope.launch {
            _eventCoordinates.value = configureEventCoordinates(value)
            eventCoordinates.collect { eventCoordinates ->
                eventCoordinates.model?.setCallback(geometryController.getCoordinatesCallback(
                    updateCoordinates = { value ->
                        setUpCoordinates(value)
                    },
                    currentLocation = {
                        requestCurrentLocation()
                    },
                    mapRequest = { _, featureType, initCoordinate ->
                        requestLocationByMap?.invoke(featureType, initCoordinate)
                    }
                ))
            }
        }
    }

    fun setupEventTemp(status: EventTempStatus? = null, isChecked: Boolean = true) {
        if (isChecked) {
            _eventTemp.value = configureEventTemp(status)
        }
    }

    fun onFieldChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    }

    fun onDateClick() {
        periodType?.let {
            showPeriods?.invoke()
        } ?: showCalendar?.invoke()
    }

    fun onDateSet(year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance()
        calendar[year, month, day, 0, 0] = 0
        calendar[Calendar.MILLISECOND] = 0
        val selectedDate = calendar.time
        onDateSet(selectedDate)
    }

    fun onDateSet(selectedDate: Date) {
        viewModelScope.launch {
            _eventDate.value = configureEventReportDate(selectedDate)
            eventDate.collect { eventDate ->
                _eventOrgUnit.value = configureOrgUnit(eventDate.currentDate)
            }
        }
    }

    fun onOrgUnitClick() {
        if (!_eventOrgUnit.value.fixed) {
            showOrgUnits?.invoke()
        }
    }

    fun onOrgUnitSet(selectedOrgUnit: String, selectedOrgUnitName: String?) {
        viewModelScope.launch {
            _eventOrgUnit.value = configureOrgUnit.invoke(selectedOrgUnit = selectedOrgUnit)
        }
    }

    fun onCategoryOptionSelected(categoryOption: Pair<String, String?>) {
        viewModelScope.launch {
            _eventCatCombo.value = configureEventCatCombo.invoke(categoryOption)
        }
    }

    fun onCatComboClick(category: EventCategory) {
        if (category.optionsSize > DEFAULT_COUNT_LIMIT) {
            showCategoryDialog?.invoke(category)
        } else {
            showCategoryPopUp?.invoke(category)
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
            }
        )
    }

    fun onLocationByMapSelected(featureType: FeatureType, coordinates: String?) {
        val geometry: Geometry? = geometryController.generateLocationFromCoordinates(
            featureType,
            coordinates
        )
        geometry?.let { setUpCoordinates(it.coordinates()) }
    }
}
