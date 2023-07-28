package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents

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
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.EventViewModelType.EVENT
import org.dhis2.commons.data.EventViewModelType.STAGE
import org.dhis2.commons.data.EventViewModelType.values
import org.dhis2.commons.data.StageSection
import org.dhis2.databinding.ItemEventBinding
import org.dhis2.databinding.ItemStageSectionBinding
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataPresenter
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.program.Program

class EventAdapter(
    val presenter: TEIDataPresenter,
    val program: Program
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
    }
) {

    private lateinit var enrollment: Enrollment

    private var stageSelector: FlowableProcessor<StageSection> = PublishProcessor.create()

    fun stageSelector(): Flowable<StageSection> {
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
                StageViewHolder(
                    binding,
                    stageSelector,
                    presenter
                )
            }
            EVENT -> {
                val binding = DataBindingUtil.inflate<ItemEventBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.item_event,
                    parent,
                    false
                )
                EventViewHolder(
                    binding,
                    program,
                    { presenter.onSyncDialogClick(it) },
                    { eventUid, sharedView -> presenter.onScheduleSelected(eventUid, sharedView) },
                    { eventUid, _, eventStatus, _ ->
                        presenter.onEventSelected(
                            eventUid,
                            eventStatus
                        )
                    }
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type.ordinal
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is EventViewHolder -> {
                holder.bind(
                    getItem(position),
                    enrollment
                ) {
                    getItem(holder.getAdapterPosition()).toggleValueList()
                    notifyItemChanged(holder.getAdapterPosition())
                }
            }
            is StageViewHolder -> {
                holder.bind(getItem(position))
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).hashCode().toLong()
    }

    fun setEnrollment(enrollment: Enrollment) {
        this.enrollment = enrollment
        this.notifyDataSetChanged()
    }
}
