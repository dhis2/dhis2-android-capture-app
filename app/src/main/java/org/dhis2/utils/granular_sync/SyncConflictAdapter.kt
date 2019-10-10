package org.dhis2.utils.granular_sync

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView

import org.dhis2.R
import org.dhis2.databinding.ItemSyncConflictBinding

class SyncConflictAdapter (private val conflicts: MutableList<StatusLogItem>) : RecyclerView.Adapter<SyncConflictHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SyncConflictHolder {
        val binding = DataBindingUtil.inflate<ItemSyncConflictBinding>(LayoutInflater.from(parent.context), R.layout.item_sync_conflict, parent, false)
        return SyncConflictHolder(binding)
    }

    override fun onBindViewHolder(holder: SyncConflictHolder, position: Int) {
        holder.bind(conflicts[position])
    }

    override fun getItemCount(): Int {
        return conflicts.size
    }

    fun addItems(conflicts: List<StatusLogItem>) {
        this.conflicts.clear()
        this.conflicts.addAll(conflicts)
        notifyDataSetChanged()
    }

    fun addItem(item: StatusLogItem) {
        this.conflicts.add(item)
        notifyDataSetChanged()
    }

    fun addAllItems(conflicts: List<StatusLogItem>) {
        this.conflicts.addAll(conflicts)
        notifyDataSetChanged()
    }
}
