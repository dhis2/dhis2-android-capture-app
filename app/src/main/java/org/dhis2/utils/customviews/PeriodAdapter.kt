package org.dhis2.utils.customviews

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList
import java.util.Date
import java.util.Locale
import org.dhis2.R
import org.dhis2.databinding.ItemDateBinding
import org.dhis2.usescases.datasets.datasetInitial.DateRangeInputPeriodModel
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.period.PeriodType

private class PeriodAdapter(
    private val periodType: PeriodType,
    openFuturePeriods: Int,
    val listener: (Date) -> Unit,
    val withInputPeriod: Boolean,
    val inputPeriods: List<DateRangeInputPeriodModel>
) : RecyclerView.Adapter<DateViewHolder>() {

    companion object {
        const val DEFAULT_PERIODS_SIZE = 10
    }

    private val datePeriods: MutableList<Date>
    private var lastDate: Date
    private var currentDate = DateUtils.getInstance().today

    init {
        datePeriods = ArrayList()
        lastDate = DateUtils.getInstance()
            .getNextPeriod(periodType, DateUtils.getInstance().today, openFuturePeriods - 1)
        if(withInputPeriod){
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
            false
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
                DateUtils.getInstance().getPeriodUIString(
                    periodType,
                    datePeriods[position],
                    Locale.getDefault()
                )
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
        }
    }

    fun setInputPeriod(){
        var isAllowed = false
        for (inputPeriodModel in inputPeriods) {
            do {
                if ((currentDate.after(inputPeriodModel.initialPeriodDate()) || currentDate == inputPeriodModel.initialPeriodDate()) && currentDate.before(
                        inputPeriodModel.endPeriodDate()
                    )
                    && (inputPeriodModel.openingDate() == null || inputPeriodModel.openingDate() != null && DateUtils.getInstance().today.after(
                        inputPeriodModel.openingDate()
                    )
                            || DateUtils.getInstance().today == inputPeriodModel.openingDate())
                    && (inputPeriodModel.closingDate() == null || inputPeriodModel.closingDate() != null && DateUtils.getInstance().today.before(
                        inputPeriodModel.closingDate()
                    ))
                ) {
                    isAllowed = true
                    datePeriods.add(currentDate)
                    lastDate = currentDate
                }
                else if (currentDate.before(inputPeriods[inputPeriods.size - 1].initialPeriodDate()) || currentDate.before(
                        inputPeriodModel.initialPeriodDate()
                    )
                )
                    break
                else {
                    currentDate = DateUtils.getInstance().getNextPeriod(periodType, currentDate, -1)
                }
            } while (!isAllowed)
            isAllowed = false
        }
    }

    override fun getItemCount() =
        if (!withInputPeriod) datePeriods.size + 1 else datePeriods.size
}
