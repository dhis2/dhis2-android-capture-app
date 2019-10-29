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
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.period.PeriodType

private class PeriodAdapter(
    private val periodType: PeriodType,
    openFuturePeriods: Int
) : RecyclerView.Adapter<DateViewHolder>() {

    companion object {
        const val DEFAULT_PERIODS_SIZE = 10
    }

    private val datePeriods: MutableList<Date>
    private var onDateSetListener: PeriodDialog.OnDateSet? = null
    private var lastDate: Date

    init {
        datePeriods = ArrayList()
        lastDate = DateUtils.getInstance()
            .getNextPeriod(periodType, DateUtils.getInstance().today, openFuturePeriods - 1)
        setDates()
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
        if (position == datePeriods.size) {
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
                onDateSetListener?.onDateSet(datePeriods[holder.adapterPosition])
            }
        }
    }

    private fun setDates() {
        repeat(DEFAULT_PERIODS_SIZE) {
            datePeriods.add(lastDate)
            lastDate = DateUtils.getInstance().getNextPeriod(periodType, lastDate, -1)
        }
    }

    override fun getItemCount(): Int {
        return datePeriods.size + 1
    }

    fun setOnDateSetListener(onDateSetListener: PeriodDialog.OnDateSet) {
        this.onDateSetListener = onDateSetListener
    }
}
