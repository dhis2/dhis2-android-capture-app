package org.dhis2.commons.filters.periods.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.commons.R
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.periods.data.FilterPeriodsRepository
import org.dhis2.commons.filters.periods.domain.GetFilterPeriods
import org.dhis2.commons.filters.periods.model.FilterPeriodType
import org.dhis2.commons.filters.periods.model.PeriodFilterType
import org.dhis2.commons.filters.periods.ui.state.FilterPeriodsScreenState
import org.dhis2.commons.periods.model.Period
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.period.DatePeriod
import java.util.Calendar

class FilterPeriodsDialogViewmodel(
    private val getFilterPeriods: GetFilterPeriods,
    private val filterPeriodsRepository: FilterPeriodsRepository,
    private val resourceManager: ResourceManager,

) : ViewModel() {

    private val _selectedPeriodType: MutableStateFlow<FilterPeriodType?> = MutableStateFlow(null)
    val selectedPeriodType: StateFlow<FilterPeriodType?> = _selectedPeriodType

    private val _periodTypes: MutableStateFlow<List<FilterPeriodType>> =
        MutableStateFlow(emptyList())

    val periodTypes: StateFlow<List<FilterPeriodType>?> = _periodTypes

    private val _showDatePicker: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val showDatePicker: StateFlow<Boolean> = _showDatePicker

    private val _dialogTitle: MutableStateFlow<String> = MutableStateFlow(
        resourceManager.getString(
            R.string.select_period,
        ),
    )
    val dialogTitle: StateFlow<String> = _dialogTitle

    private var periodFilterType: PeriodFilterType = PeriodFilterType.OTHER

    private var isDataSetFilter: Boolean = false

    private val _filterPeriodsScreenState =
        MutableStateFlow<FilterPeriodsScreenState>(FilterPeriodsScreenState.Loading)
    val filterPeriodsScreenState = _filterPeriodsScreenState
        .onStart { loadPeriodTypes() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            FilterPeriodsScreenState.Loading,
        )

    init {
        viewModelScope.launch {
        }
    }

    private fun loadPeriodTypes() {
        val periodTypes = if (!isDataSetFilter) filterPeriodsRepository.getDefaultPeriodTypes() else filterPeriodsRepository.getDataSetFilterPeriodTypes()
        _periodTypes.value = periodTypes
        _filterPeriodsScreenState.update {
            FilterPeriodsScreenState.Loaded(
                periodTypes = periodTypes,
                periods = emptyList(),
                selectedPeriodType = null,
            )
        }
    }

    fun onPeriodTypeSelected(periodType: FilterPeriodType) {
        viewModelScope.launch {
            _selectedPeriodType.value = periodType
            if (selectedPeriodType.value == FilterPeriodType.DAILY) {
                _showDatePicker.value = true
            }
            _dialogTitle.value = selectedPeriodType.value?.nameResource?.let {
                resourceManager.getString(
                    it,
                )
            }
                ?: ""
            _filterPeriodsScreenState.update {
                FilterPeriodsScreenState.Loaded(
                    periodTypes = periodTypes.value ?: emptyList(),
                    periods = emptyList(),
                    selectedPeriodType = periodType,
                )
            }
        }
    }

    fun fetchPeriods(): Flow<PagingData<Period>> {
        return getFilterPeriods(
            filterPeriodType = selectedPeriodType.value!!,
        )
    }

    fun onPeriodSelected(period: Period) {
        when (periodFilterType) {
            PeriodFilterType.ENROLLMENT_DATE -> {
                FilterManager.getInstance().addEnrollmentPeriod(listOf(DatePeriod.create(period.startDate, period.endDate)))
            }
            PeriodFilterType.EVENT_DATE, PeriodFilterType.OTHER -> {
                FilterManager.getInstance().addPeriod(listOf(DatePeriod.create(period.startDate, period.endDate)))
            }
        }
    }

    fun setFilterType(periodFilterType: PeriodFilterType) {
        this.periodFilterType = periodFilterType
    }

    fun setDailyPeriodFilter(selectedDateMillis: Long?) {
        val selectedDate = Calendar.getInstance()
        selectedDateMillis?.let {
            selectedDate.timeInMillis = selectedDateMillis
        }
        val datePeriods = mutableListOf(DatePeriod.create(selectedDate.time, selectedDate.time))
        when (periodFilterType) {
            PeriodFilterType.ENROLLMENT_DATE -> {
                FilterManager.getInstance().addEnrollmentPeriod(datePeriods)
            }
            PeriodFilterType.EVENT_DATE, PeriodFilterType.OTHER -> {
                FilterManager.getInstance().addPeriod(datePeriods)
            }
        }
    }

    fun setFilterPeriodTypes(isDataSetFilters: Boolean) {
        this.isDataSetFilter = isDataSetFilters
    }

    fun setFromToFilter(fromSelectedDateMillis: Long?, toSelectedDateMillis: Long?) {
        val fromSelectedDate = Calendar.getInstance()
        fromSelectedDateMillis?.let {
            fromSelectedDate.timeInMillis = it
        }
        val toSelectedDate = Calendar.getInstance()
        toSelectedDateMillis?.let {
            toSelectedDate.timeInMillis = it
        }
        val datePeriods = mutableListOf(DatePeriod.create(fromSelectedDate.time, toSelectedDate.time))
        when (periodFilterType) {
            PeriodFilterType.ENROLLMENT_DATE -> {
                FilterManager.getInstance().addEnrollmentPeriod(datePeriods)
            }
            PeriodFilterType.EVENT_DATE, PeriodFilterType.OTHER -> {
                FilterManager.getInstance().addPeriod(datePeriods)
            }
        }
    }
}
