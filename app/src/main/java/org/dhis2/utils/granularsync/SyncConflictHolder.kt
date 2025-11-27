package org.dhis2.utils.granularsync

import androidx.recyclerview.widget.RecyclerView
import org.dhis2.commons.date.DateUtils
import org.dhis2.databinding.ItemSyncConflictBinding

class SyncConflictHolder(
    private val binding: ItemSyncConflictBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(trackerImportConflict: StatusLogItem) {
        binding.date.text = DateUtils.dateTimeFormat().format(trackerImportConflict.date)
        binding.message.text = trackerImportConflict.description
    }
}
