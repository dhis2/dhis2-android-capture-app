package org.dhis2.commons.filters.periods.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.commons.R
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.Filters
import org.dhis2.commons.filters.periods.data.PeriodTypeLabelProvider
import org.dhis2.commons.filters.periods.domain.GetFilterPeriodTypes
import org.dhis2.commons.filters.periods.domain.GetFilterPeriods
import org.dhis2.commons.filters.periods.model.FilterPeriodType
import org.dhis2.commons.filters.periods.ui.FilterPeriodsDialog.FilterDialogLaunchMode
import org.dhis2.commons.filters.periods.ui.state.FilterPeriodsScreenState
import org.dhis2.commons.periods.model.Period
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.mobile.commons.coroutine.CoroutineTracker
import org.hisp.dhis.android.core.period.DatePeriod
import java.util.Calendar

class FilterPeriodsDialogViewmodel(
    private val getFilterPeriods: GetFilterPeriods,
    private val getFilterPeriodTypes: GetFilterPeriodTypes,
    private val resourceManager: ResourceManager,
    private val periodTypeLabelProvider: PeriodTypeLabelProvider,
    private val launchMode: FilterDialogLaunchMode,
) : ViewModel() {
    private val _filterPeriodsScreenState =
        MutableStateFlow(
            FilterPeriodsScreenState(
                periodTypes = emptyList(),
                periods = emptyList(),
                selectedPeriodType = null,
                title =
                    resourceManager.getString(
                        R.string.select_period,
                    ),
            ),
        )
    val filterPeriodsScreenState = _filterPeriodsScreenState.asStateFlow()

    init {
        viewModelScope.launch {
            loadPeriodTypes()
        }
    }

    private fun loadPeriodTypes() {
        val periodTypes = getFilterPeriodTypes(launchMode is FilterDialogLaunchMode.NewDataSetPeriodDialog)

        _filterPeriodsScreenState.update {
            filterPeriodsScreenState.value.copy(
                periodTypes = periodTypes,
            )
        }
    }

    fun onPeriodTypeSelected(periodType: FilterPeriodType) {
        viewModelScope.launch {
            if (periodType == FilterPeriodType.DAILY) {
                _filterPeriodsScreenState.update {
                    filterPeriodsScreenState.value.copy(
                        showDatePicker = true,
                    )
                }
            } else {
                _filterPeriodsScreenState.update {
                    filterPeriodsScreenState.value.copy(
                        selectedPeriodType = periodType,
                        title =
                            resourceManager.getString(
                                periodTypeLabelProvider.invoke(periodType),
                            ),
                    )
                }
            }
        }
    }

    fun fetchPeriods(): Flow<PagingData<Period>> =
        getFilterPeriods(
            filterPeriodType = _filterPeriodsScreenState.value.selectedPeriodType!!,
        )

    fun onPeriodSelected(period: Period) {
        viewModelScope.launch {
            CoroutineTracker.increment()
            when (launchMode) {
                is FilterDialogLaunchMode.NewPeriodDialog -> {
                    if (launchMode.filterType == Filters.PERIOD) {
                        FilterManager.getInstance().addPeriod(
                            listOf(
                                DatePeriod.create(
                                    period.startDate,
                                    period.endDate,
                                ),
                            ),
                        )
                    } else {
                        FilterManager.getInstance().addEnrollmentPeriod(
                            listOf(
                                DatePeriod.create(
                                    period.startDate,
                                    period.endDate,
                                ),
                            ),
                        )
                    }
                }

                is FilterDialogLaunchMode.NewDataSetPeriodDialog -> {
                    FilterManager
                        .getInstance()
                        .addPeriod(listOf(DatePeriod.create(period.startDate, period.endDate)))
                }
            }
            CoroutineTracker.decrement()
        }
    }

    fun setDailyPeriodFilter(selectedDateMillis: Long?) {
        viewModelScope.launch {
            CoroutineTracker.increment()
            val selectedDate = Calendar.getInstance()
            selectedDateMillis?.let {
                selectedDate.timeInMillis = selectedDateMillis
            }
            val datePeriods = mutableListOf(DatePeriod.create(selectedDate.time, selectedDate.time))
            when (launchMode) {
                is FilterDialogLaunchMode.NewPeriodDialog -> {
                    if (launchMode.filterType == Filters.ENROLLMENT_DATE) {
                        FilterManager.getInstance().addEnrollmentPeriod(datePeriods)
                    } else {
                        FilterManager.getInstance().addPeriod(datePeriods)
                    }
                }

                else -> {
                    FilterManager.getInstance().addPeriod(datePeriods)
                }
            }
            CoroutineTracker.decrement()
        }
    }

    fun setFromToFilter(
        fromSelectedDateMillis: Long?,
        toSelectedDateMillis: Long?,
    ) {
        viewModelScope.launch {
            CoroutineTracker.increment()
            val fromSelectedDate = Calendar.getInstance()
            fromSelectedDateMillis?.let {
                fromSelectedDate.timeInMillis = it
            }
            val toSelectedDate = Calendar.getInstance()
            toSelectedDateMillis?.let {
                toSelectedDate.timeInMillis = it
            }
            val datePeriods =
                mutableListOf(DatePeriod.create(fromSelectedDate.time, toSelectedDate.time))
            when (launchMode) {
                is FilterDialogLaunchMode.NewPeriodDialog -> {
                    if (launchMode.filterType == Filters.ENROLLMENT_DATE) {
                        FilterManager.getInstance().addEnrollmentPeriod(datePeriods)
                    } else {
                        FilterManager.getInstance().addPeriod(datePeriods)
                    }
                }

                else -> {
                    FilterManager.getInstance().addPeriod(datePeriods)
                }
            }
            CoroutineTracker.decrement()
        }
    }

    fun getPeriodTypeName(filterPeriodType: FilterPeriodType): String = resourceManager.getString(periodTypeLabelProvider(filterPeriodType))
}
