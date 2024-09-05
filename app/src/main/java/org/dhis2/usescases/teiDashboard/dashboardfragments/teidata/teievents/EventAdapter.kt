package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalTextStyle
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Flowable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import org.dhis2.R
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.EventViewModelType
import org.dhis2.commons.data.EventViewModelType.EVENT
import org.dhis2.commons.data.EventViewModelType.STAGE
import org.dhis2.commons.data.EventViewModelType.TOGGLE_BUTTON
import org.dhis2.commons.data.StageSection
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.databinding.ItemEventBinding
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataPresenter
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.ui.mapper.TEIEventCardMapper
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardDescriptionModel
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

class EventAdapter(
    val presenter: TEIDataPresenter,
    val program: Program,
    val colorUtils: ColorUtils,
    private val cardMapper: TEIEventCardMapper,
) : ListAdapter<EventViewModel, RecyclerView.ViewHolder>(
    object : DiffUtil.ItemCallback<EventViewModel>() {
        override fun areItemsTheSame(oldItem: EventViewModel, newItem: EventViewModel): Boolean {
            val oldItemId = if (oldItem.type == EVENT) {
                oldItem.event!!.uid()
            } else {
                oldItem.stage!!.uid()
            }
            val newItemId = if (newItem.type == EVENT) {
                newItem.event!!.uid()
            } else {
                newItem.stage!!.uid()
            }
            return oldItemId == newItemId
        }

        override fun areContentsTheSame(oldItem: EventViewModel, newItem: EventViewModel): Boolean {
            return oldItem == newItem
        }
    },
) {

    private var stageSelector: FlowableProcessor<StageSection> = PublishProcessor.create()

    fun stageSelector(): Flowable<StageSection> {
        return stageSelector
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (EventViewModelType.entries[viewType]) {
            STAGE -> {
                StageViewHolder(
                    ComposeView(parent.context),
                    stageSelector,
                    presenter,
                    colorUtils,
                )
            }

            EVENT -> {
                val binding = DataBindingUtil.inflate<ItemEventBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.item_event,
                    parent,
                    false,
                )
                EventViewHolder(
                    binding,
                    program,
                    colorUtils,
                    { presenter.onSyncDialogClick(it) },
                    { eventUid, sharedView -> presenter.onScheduleSelected(eventUid, sharedView) },
                    { eventUid, _, eventStatus, _ ->
                        presenter.onEventSelected(
                            eventUid,
                            eventStatus,
                        )
                    },
                )
            }

            TOGGLE_BUTTON -> {
                ToggleStageEventsButtonHolder(
                    ComposeView(parent.context),
                    stageSelector,
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
                getItem(position)?.let {
                    val materialView = holder.itemView.findViewById<ConstraintLayout>(R.id.materialView)
                    materialView.visibility = View.GONE
                    val composeView = holder.itemView.findViewById<ComposeView>(R.id.composeView)
                    composeView.setContent {
                        val leftSpacing = if (it.groupedByStage == true) Spacing.Spacing64 else Spacing.Spacing16
                        val bottomSpacing = if (it.showBottomShadow) {
                            Spacing.Spacing16
                        } else {
                            Spacing.Spacing4
                        }
                        val card = cardMapper.map(
                            event = it,
                            editable = it.editable,
                            displayOrgUnit = it.displayOrgUnit,
                            onCardClick = {
                                it.event?.let { event ->
                                    when (event.status()) {
                                        EventStatus.SCHEDULE, EventStatus.OVERDUE, EventStatus.SKIPPED -> {
                                            presenter.onScheduleSelected(
                                                event.uid(),
                                                composeView,
                                            )
                                        }

                                        else -> {
                                            presenter.onEventSelected(
                                                event.uid(),
                                                event.status()!!,
                                            )
                                        }
                                    }
                                }
                            },
                        )
                        Box(
                            modifier = Modifier
                                .padding(
                                    start = leftSpacing,
                                    end = Spacing.Spacing16,
                                    bottom = bottomSpacing,
                                ),
                        ) {
                            ListCard(
                                listAvatar = card.avatar,
                                title = ListCardTitleModel(
                                    text = card.title,
                                    style = LocalTextStyle.current.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight(500),
                                        lineHeight = 20.sp,
                                    ),
                                    color = TextColor.OnSurface,
                                ),
                                description = ListCardDescriptionModel(
                                    text = card.description,
                                ),
                                lastUpdated = card.lastUpdated,
                                additionalInfoList = card.additionalInfo,
                                actionButton = card.actionButton,
                                expandLabelText = card.expandLabelText,
                                shrinkLabelText = card.shrinkLabelText,
                                onCardClick = card.onCardCLick,
                            )
                        }
                    }

                    holder.bind(it, null) {
                        getItem(holder.bindingAdapterPosition)?.toggleValueList()
                        notifyItemChanged(holder.bindingAdapterPosition)
                    }
                }
            }

            is StageViewHolder -> {
                holder.bind(getItem(position))
            }

            is ToggleStageEventsButtonHolder -> {
                holder.bind(getItem(position))
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).hashCode().toLong()
    }
}
