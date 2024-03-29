package org.dhis2.utils.granularsync

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.R
import org.dhis2.databinding.ItemSyncConflictBinding
import java.lang.ref.WeakReference

class SyncConflictAdapter(
    private val conflicts: MutableList<StatusLogItem>,
    private val showErrorLog: () -> Unit,
) : RecyclerView.Adapter<SyncConflictHolder>() {

    lateinit var recycler: WeakReference<RecyclerView>

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recycler = WeakReference(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SyncConflictHolder {
        val binding = DataBindingUtil.inflate<ItemSyncConflictBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_sync_conflict,
            parent,
            false,
        )
        return SyncConflictHolder(binding)
    }

    override fun onBindViewHolder(holder: SyncConflictHolder, position: Int) {
        holder.bind(conflicts[position])
        if (conflicts[position].openLogs()) {
            holder.itemView.setOnClickListener {
                showErrorLog()
            }
        } else {
            holder.itemView.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int {
        return conflicts.size
    }

    fun addItems(conflicts: List<StatusLogItem>) {
        this.conflicts.clear()
        this.conflicts.addAll(conflicts)
        notifyDataSetChanged()
        scrollToLastItem()
    }

    fun addItem(item: StatusLogItem) {
        this.conflicts.add(item)
        notifyDataSetChanged()
        scrollToLastItem()
    }

    fun addAllItems(conflicts: List<StatusLogItem>) {
        this.conflicts.addAll(conflicts)
        notifyDataSetChanged()
        scrollToLastItem()
    }

    private fun scrollToLastItem() {
        if (conflicts.isNotEmpty()) {
            recycler.get()?.smoothScrollToPosition(conflicts.size - 1)
        }
    }
}
