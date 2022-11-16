package org.dhis2.usescases.programEventDetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import org.dhis2.commons.data.EventViewModel
import org.dhis2.databinding.ItemEventBinding
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewHolder
import org.hisp.dhis.android.core.program.Program

class ProgramEventDetailLiveAdapter(
    private val program: Program,
    private val eventViewModel: ProgramEventDetailViewModel,
    config: AsyncDifferConfig<EventViewModel>
) : PagedListAdapter<EventViewModel, EventViewHolder>(config) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemEventBinding.inflate(inflater, parent, false)
        return EventViewHolder(
            binding,
            program,
            { eventUid ->
                eventViewModel.eventSyncClicked.value = eventUid
            },
            { _, _ -> },
            { eventUid, orgUnitUid, _, _ ->
                eventViewModel.eventClicked.value = Pair(eventUid, orgUnitUid)
            }
        )
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position), null) {
            getItem(holder.bindingAdapterPosition)?.toggleValueList()
            notifyItemChanged(holder.bindingAdapterPosition)
        }
    }

    companion object {
        val diffCallback: DiffUtil.ItemCallback<EventViewModel>
            get() = object : DiffUtil.ItemCallback<EventViewModel>() {
                override fun areItemsTheSame(
                    oldItem: EventViewModel,
                    newItem: EventViewModel
                ): Boolean {
                    return oldItem.event?.uid() == newItem.event?.uid()
                }

                override fun areContentsTheSame(
                    oldItem: EventViewModel,
                    newItem: EventViewModel
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }
}
