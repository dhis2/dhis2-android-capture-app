package org.dhis2.commons.filters.dates

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CompoundButton
import android.widget.FrameLayout
import androidx.databinding.Observable
import org.dhis2.commons.R
import org.dhis2.commons.databinding.FilterPeriodBinding
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.date.Period
import org.dhis2.commons.filters.EnrollmentDateFilter
import org.dhis2.commons.filters.FilterItem
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.PeriodFilter
import org.hisp.dhis.android.core.period.DatePeriod
import java.util.Calendar
import java.util.Date

class FilterPeriodView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr), CompoundButton.OnCheckedChangeListener {
    private val binding =
        FilterPeriodBinding.inflate(LayoutInflater.from(context), this, true)
    private var periodRequest: (FilterManager.PeriodRequest, Int) -> Unit =
        { _, _ -> }
    private var onPeriodSelected: (List<DatePeriod>, Int) -> Unit = { _, _ -> }

    init {
        setListeners()
    }

    fun setFilterItem(filterItem: FilterItem) {
        binding.filterItem = filterItem
        updateSelection(R.id.anytime)
        if (filterItem is EnrollmentDateFilter) {
            setEnrollmentFilter(filterItem)
        } else if (filterItem is PeriodFilter) {
            setPeriodFilter(filterItem)
        }
    }

    private fun setEnrollmentFilter(filterItem: EnrollmentDateFilter) {
        filterItem.observePeriod()
            .addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    updateSelection(filterItem.observePeriod().get() ?: R.id.anytime)
                }
            })
        periodRequest = { periodRequest, checkedId ->
            updateSelection(checkedId)
            filterItem.requestPeriod(periodRequest, checkedId)
        }
        onPeriodSelected = { periods, checkedId ->
            filterItem.setSelectedPeriod(periods, checkedId)
        }
    }

    private fun setPeriodFilter(filterItem: PeriodFilter) {
        filterItem.observePeriod()
            .addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    updateSelection(filterItem.observePeriod().get() ?: R.id.anytime)
                }
            })
        periodRequest = { periodRequest, checkedId ->
            updateSelection(checkedId)
            filterItem.requestPeriod(periodRequest, checkedId)
        }

        onPeriodSelected = { periods, checkedId ->
            filterItem.setSelectedPeriod(periods, checkedId)
        }
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, isChecked: Boolean) {
        if (isChecked) {
            val id: Int = compoundButton.id
            updateSelection(id)
            if (id != R.id.other && id != R.id.fromTo) {
                var dates: Array<Date?>? = null
                val calendar = Calendar.getInstance()
                when (id) {
                    R.id.today ->
                        dates = DateUtils.getInstance()
                            .getDateFromDateAndPeriod(calendar.time, Period.DAILY)
                    R.id.yesterday -> {
                        calendar.add(Calendar.DAY_OF_YEAR, -1)
                        dates = DateUtils.getInstance().getDateFromDateAndPeriod(
                            calendar.time,
                            Period.DAILY,
                        )
                    }
                    R.id.tomorrow -> {
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                        dates = DateUtils.getInstance().getDateFromDateAndPeriod(
                            calendar.time,
                            Period.DAILY,
                        )
                    }
                    R.id.this_week ->
                        dates = DateUtils.getInstance()
                            .getDateFromDateAndPeriod(calendar.time, Period.WEEKLY)
                    R.id.last_week -> {
                        calendar.add(Calendar.WEEK_OF_YEAR, -1)
                        dates = DateUtils.getInstance().getDateFromDateAndPeriod(
                            calendar.time,
                            Period.WEEKLY,
                        )
                    }
                    R.id.next_week -> {
                        calendar.add(Calendar.WEEK_OF_YEAR, 1)
                        dates = DateUtils.getInstance().getDateFromDateAndPeriod(
                            calendar.time,
                            Period.WEEKLY,
                        )
                    }
                    R.id.this_month ->
                        dates = DateUtils.getInstance()
                            .getDateFromDateAndPeriod(
                                calendar.time,
                                Period.MONTHLY,
                            )
                    R.id.last_month -> {
                        calendar.add(Calendar.MONTH, -1)
                        dates = DateUtils.getInstance().getDateFromDateAndPeriod(
                            calendar.time,
                            Period.MONTHLY,
                        )
                    }
                    R.id.next_month -> {
                        calendar.add(Calendar.MONTH, 1)
                        dates = DateUtils.getInstance().getDateFromDateAndPeriod(
                            calendar.time,
                            Period.MONTHLY,
                        )
                    }
                }

                val periods = if (dates != null) {
                    mutableListOf(
                        DatePeriod.builder().startDate(dates[0]).endDate(dates[1])
                            .build(),
                    )
                } else {
                    mutableListOf()
                }
                onPeriodSelected(periods, id)
            }
        }
    }

    private fun updateSelection(id: Int) {
        binding.today.isChecked = id == R.id.today
        binding.yesterday.isChecked = id == R.id.yesterday
        binding.tomorrow.isChecked = id == R.id.tomorrow
        binding.thisWeek.isChecked = id == R.id.this_week
        binding.lastWeek.isChecked = id == R.id.last_week
        binding.nextWeek.isChecked = id == R.id.next_week
        binding.thisMonth.isChecked = id == R.id.this_month
        binding.lastMonth.isChecked = id == R.id.last_month
        binding.nextMonth.isChecked = id == R.id.next_month
        binding.fromTo.isChecked = id == R.id.fromTo
        binding.other.isChecked = id == R.id.other
        binding.anytime.isChecked = id == R.id.anytime
    }

    private fun setListeners() {
        binding.today.setOnCheckedChangeListener(this)
        binding.yesterday.setOnCheckedChangeListener(this)
        binding.tomorrow.setOnCheckedChangeListener(this)
        binding.thisWeek.setOnCheckedChangeListener(this)
        binding.lastWeek.setOnCheckedChangeListener(this)
        binding.nextWeek.setOnCheckedChangeListener(this)
        binding.thisMonth.setOnCheckedChangeListener(this)
        binding.lastMonth.setOnCheckedChangeListener(this)
        binding.nextMonth.setOnCheckedChangeListener(this)
        binding.fromTo.setOnClickListener {
            if (binding.fromTo.isChecked) {
                periodRequest(FilterManager.PeriodRequest.FROM_TO, R.id.fromTo)
            }
        }
        binding.other.setOnClickListener {
            if (binding.other.isChecked) {
                periodRequest(FilterManager.PeriodRequest.OTHER, R.id.other)
            }
        }
        binding.anytime.setOnCheckedChangeListener(this)
    }
}
