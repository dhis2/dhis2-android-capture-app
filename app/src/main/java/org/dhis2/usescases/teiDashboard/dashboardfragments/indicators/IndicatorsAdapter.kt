package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.R
import org.dhis2.data.tuples.Trio
import org.dhis2.databinding.ItemIndicatorBinding
import org.hisp.dhis.android.core.program.ProgramIndicator

class IndicatorsAdapter : RecyclerView.Adapter<IndicatorViewHolder>() {

    private var programIndicators:
        MutableList<Trio<ProgramIndicator, String, String>> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IndicatorViewHolder {
        val binding: ItemIndicatorBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_indicator,
            parent,
            false
        )

        return IndicatorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IndicatorViewHolder, position: Int) {
        holder.bind(programIndicators[position])
    }

    override fun getItemCount() = programIndicators.size

    fun setIndicators(indicators: List<Trio<ProgramIndicator, String, String>>) {
        programIndicators.clear()
        programIndicators.addAll(indicators)
        notifyDataSetChanged()
    }
}
