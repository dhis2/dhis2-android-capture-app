package org.dhis2.usescases.reservedValue

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.databinding.ItemReservedValueBinding

class ReservedValueAdapter :
    ListAdapter<ReservedValueModel, ReservedValueAdapter.ReservedValueViewHolder>(object :
        DiffUtil.ItemCallback<ReservedValueModel>() {
        override fun areItemsTheSame(
            oldItem: ReservedValueModel,
            newItem: ReservedValueModel,
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: ReservedValueModel,
            newItem: ReservedValueModel,
        ): Boolean {
            return oldItem == newItem
        }
    }) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ReservedValueViewHolder {
        val binding = ItemReservedValueBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false,
        )
        return ReservedValueViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReservedValueViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReservedValueViewHolder(private val binding: ItemReservedValueBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(reservedValue: ReservedValueModel) {
            binding.reservedValueModel = reservedValue
        }
    }
}
