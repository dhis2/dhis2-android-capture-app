package org.dhis2.utils.customviews

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.R
import org.dhis2.data.dhislogic.DhisPeriodUtils
import org.dhis2.databinding.ItemDateBinding
import org.dhis2.usescases.datasets.datasetInitial.DateRangeInputPeriodModel
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.PeriodType
import java.util.ArrayList
import java.util.Date
import java.util.Locale

private class PeriodAdapter(
    private val periodType: PeriodType,
    val openFuturePeriods: Int,
    val listener: (Date) -> Unit,
    var withInputPeriod: Boolean,
    val organisationUnit: OrganisationUnit?,
    val inputPeriods: List<DateRangeInputPeriodModel>,
    val periodUtils: DhisPeriodUtils,
) : RecyclerView.Adapter<DateViewHolder>() {

    companion object {
        const val DEFAULT_PERIODS_SIZE = 10
    }

    private val datePeriods: MutableList<Date>
    private var lastDate: Date
    private var currentDate = DateUtils.getInstance().today

    init {
        datePeriods = ArrayList()
        lastDate = DateUtils.getInstance().getNextPeriod(
            periodType,
            organisationUnit?.closedDate() ?: DateUtils.getInstance().today,
            openFuturePeriods - 1,
        )
        if (withInputPeriod) {
            setInputPeriod()
        } else {
            setDates()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val binding = DataBindingUtil.inflate<ItemDateBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_date,
            parent,
            false,
        )
        return DateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        if (position == datePeriods.size && !withInputPeriod) {
            holder.bind(holder.itemView.context.getString(R.string.view_more))

            holder.itemView.setOnClickListener {
                setDates()
                notifyDataSetChanged()
            }
        } else {
            holder.bind(
                periodUtils.getPeriodUIString(
                    periodType,
                    datePeriods[position],
                    Locale.getDefault(),
                ),
            )
            holder.itemView.setOnClickListener {
                listener(datePeriods[holder.adapterPosition])
            }
        }
    }

    private fun setDates() {
        repeat(DEFAULT_PERIODS_SIZE) {
            datePeriods.add(lastDate)
            lastDate = DateUtils.getInstance().getNextPeriod(periodType, lastDate, -1)
            if (organisationUnit?.openingDate()?.after(lastDate) == true) {
                withInputPeriod = true
                return
            }
        }
    }

    fun setInputPeriod() {
        var isAllowed = false

        for (inputPeriodModel in inputPeriods) {
            do {
                if (isTodayRightDayForInputPeriod(inputPeriodModel)) {
                    isAllowed = true
                    datePeriods.add(inputPeriodModel.initialPeriodDate() ?: currentDate)
                    lastDate = currentDate
                } else if (
                    currentDate.before(inputPeriods[inputPeriods.size - 1].initialPeriodDate()) ||
                    currentDate.before(inputPeriodModel.initialPeriodDate())
                ) {
                    break
                } else {
                    currentDate = DateUtils.getInstance().getNextPeriod(periodType, currentDate, -1)
                }
            } while (!isAllowed)
            isAllowed = false
        }
    }

    fun isTodayRightDayForInputPeriod(inputPeriodModel: DateRangeInputPeriodModel): Boolean {
        val isFuturePeriodsConfigured = openFuturePeriods > 0
        val isPeriodInTheFuture =
            inputPeriodModel.initialPeriodDate() != null &&
                DateUtils.getInstance().today.before(inputPeriodModel.initialPeriodDate())

        val hasNotExpired =
            currentDate.after(inputPeriodModel.initialPeriodDate()) ||
                currentDate == inputPeriodModel.initialPeriodDate() &&
                currentDate.before(inputPeriodModel.endPeriodDate())

        val isInsideOpenDates =
            inputPeriodModel.openingDate() == null ||
                inputPeriodModel.openingDate() != null &&
                DateUtils.getInstance().today.after(inputPeriodModel.openingDate()) ||
                DateUtils.getInstance().today == inputPeriodModel.openingDate()

        val isInsideCloseDates =
            inputPeriodModel.closingDate() == null ||
                inputPeriodModel.closingDate() != null &&
                DateUtils.getInstance().today.before(inputPeriodModel.closingDate())

        return if (isFuturePeriodsConfigured && isPeriodInTheFuture) {
            val isInsideInitialPeriodDate =
                DateUtils.getInstance()
                    .isInsideFutureInputPeriod(inputPeriodModel, openFuturePeriods)

            isInsideInitialPeriodDate && isInsideOpenDates && isInsideCloseDates
        } else {
            hasNotExpired && isInsideOpenDates && isInsideCloseDates
        }
    }

    override fun getItemCount() = if (!withInputPeriod) datePeriods.size + 1 else datePeriods.size
}
