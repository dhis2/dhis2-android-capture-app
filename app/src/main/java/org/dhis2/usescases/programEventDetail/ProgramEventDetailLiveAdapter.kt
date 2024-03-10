package org.dhis2.usescases.programEventDetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.testTag
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import org.dhis2.R
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.databinding.ItemEventBinding
import org.dhis2.usescases.programEventDetail.eventList.ui.mapper.EventCardMapper
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewHolder
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel

class ProgramEventDetailLiveAdapter(
    private val program: Program,
    private val eventViewModel: ProgramEventDetailViewModel,
    private val colorUtils: ColorUtils,
    private val cardMapper: EventCardMapper,
    config: AsyncDifferConfig<EventViewModel>,
) : PagedListAdapter<EventViewModel, EventViewHolder>(config) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemEventBinding.inflate(inflater, parent, false)
        return EventViewHolder(
            binding,
            program,
            colorUtils,
            { eventUid ->
                eventViewModel.eventSyncClicked.value = eventUid
            },
            { _, _ -> },
            { eventUid, orgUnitUid, _, _ ->
                eventViewModel.eventClicked.value = Pair(eventUid, orgUnitUid)
            },
        )
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        getItem(position)?.let {
            val materialView = holder.itemView.findViewById<ConstraintLayout>(R.id.materialView)
            materialView.visibility = View.GONE
            val composeView = holder.itemView.findViewById<ComposeView>(R.id.composeView)
            composeView.setContent {
                val card = cardMapper.map(
                    event = it,
                    editable = it.event?.uid()
                        ?.let { eventViewModel.isEditable(it) } ?: true,
                    displayOrgUnit = it.event?.program()
                        ?.let { program -> eventViewModel.displayOrganisationUnit(program) }
                        ?: true,
                    onSyncIconClick = {
                        eventViewModel.eventSyncClicked.value = it.event?.uid()
                    },
                    onCardClick = {
                        it.event?.let { event ->
                            eventViewModel.eventClicked.value =
                                Pair(event.uid(), event.organisationUnit() ?: "")
                        }
                    },
                )
                ListCard(
                    modifier = Modifier.testTag("EVENT_ITEM"),
                    listAvatar = card.avatar,
                    title = ListCardTitleModel(text = card.title),
                    lastUpdated = card.lastUpdated,
                    additionalInfoList = card.additionalInfo,
                    actionButton = card.actionButton,
                    expandLabelText = card.expandLabelText,
                    shrinkLabelText = card.shrinkLabelText,
                    onCardClick = card.onCardCLick,
                )
            }

            holder.bind(it, null) {
                getItem(holder.bindingAdapterPosition)?.toggleValueList()
                notifyItemChanged(holder.bindingAdapterPosition)
            }
        }
    }

    companion object {
        val diffCallback: DiffUtil.ItemCallback<EventViewModel>
            get() = object : DiffUtil.ItemCallback<EventViewModel>() {
                override fun areItemsTheSame(
                    oldItem: EventViewModel,
                    newItem: EventViewModel,
                ): Boolean {
                    return oldItem.event?.uid() == newItem.event?.uid()
                }

                override fun areContentsTheSame(
                    oldItem: EventViewModel,
                    newItem: EventViewModel,
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }
}
