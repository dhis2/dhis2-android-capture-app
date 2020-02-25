package org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Flowable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import org.dhis2.R
import org.dhis2.databinding.ItemEventBinding
import org.dhis2.databinding.ItemStageSectionBinding
import org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data.EventViewModelType.EVENT
import org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data.EventViewModelType.STAGE
import org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data.EventViewModelType.values
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.program.Program

class EventAdapter(
    val presenter: TEIDataContracts.Presenter,
    val program: Program,
    val enrollment: Enrollment
) : ListAdapter<EventViewModel, RecyclerView.ViewHolder>(
    object : DiffUtil.ItemCallback<EventViewModel>() {
        override fun areItemsTheSame(oldItem: EventViewModel, newItem: EventViewModel): Boolean {
            val oldItemId = if (oldItem.type == STAGE) {
                oldItem.stage!!.uid()
            } else {
                oldItem.event!!.uid()
            }
            val newItemId = if (newItem.type == STAGE) {
                newItem.stage!!.uid()
            } else {
                newItem.event!!.uid()
            }
            return oldItemId == newItemId
        }

        override fun areContentsTheSame(oldItem: EventViewModel, newItem: EventViewModel): Boolean {
            return oldItem == newItem
        }
    }) {

    private var stageSelector: FlowableProcessor<String> = PublishProcessor.create()

    fun stageSelector(): Flowable<String> {
        return stageSelector
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (values()[viewType]) {
            STAGE -> {
                val binding = ItemStageSectionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                StageViewHolder(binding, stageSelector, presenter)
            }
            EVENT -> {
                val binding = DataBindingUtil.inflate<ItemEventBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.item_event,
                    parent,
                    false
                )
                EventViewHolder(binding, program, enrollment, presenter)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type.ordinal
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is EventViewHolder -> {
                holder.bind(getItem(position))
            }
            is StageViewHolder -> {
                holder.bind(getItem(position))
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).hashCode().toLong()
    }
}