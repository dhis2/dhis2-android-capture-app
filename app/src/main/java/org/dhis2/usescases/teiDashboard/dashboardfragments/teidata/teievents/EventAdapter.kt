package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
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
import org.dhis2.commons.data.EventModel
import org.dhis2.commons.data.EventViewModelType
import org.dhis2.commons.data.EventViewModelType.EVENT
import org.dhis2.commons.data.EventViewModelType.STAGE
import org.dhis2.commons.data.EventViewModelType.TOGGLE_BUTTON
import org.dhis2.commons.data.StageSection
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.ui.ListCardProvider
import org.dhis2.databinding.ItemEventBinding
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataPresenter
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.ui.mapper.TEIEventCardMapper
import org.dhis2.utils.isLandscape
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.theme.Radius
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

class EventAdapter(
    val presenter: TEIDataPresenter,
    val program: Program,
    val colorUtils: ColorUtils,
    private val cardMapper: TEIEventCardMapper,
    private val initialSelectedEventUid: String? = null,
) : ListAdapter<EventModel, RecyclerView.ViewHolder>(
        object : DiffUtil.ItemCallback<EventModel>() {
            override fun areItemsTheSame(
                oldItem: EventModel,
                newItem: EventModel,
            ): Boolean {
                val oldItemId =
                    if (oldItem.type == EVENT) {
                        oldItem.event!!.uid()
                    } else {
                        oldItem.stage!!.uid()
                    }
                val newItemId =
                    if (newItem.type == EVENT) {
                        newItem.event!!.uid()
                    } else {
                        newItem.stage!!.uid()
                    }
                return oldItemId == newItemId
            }

            override fun areContentsTheSame(
                oldItem: EventModel,
                newItem: EventModel,
            ): Boolean = oldItem == newItem
        },
    ) {
    private var stageSelector: FlowableProcessor<StageSection> = PublishProcessor.create()

    private var previousSelectedPosition: Int = RecyclerView.NO_POSITION

    fun stageSelector(): Flowable<StageSection> = stageSelector

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder =
        when (EventViewModelType.entries[viewType]) {
            STAGE -> {
                StageViewHolder(
                    ComposeView(parent.context),
                    stageSelector,
                    presenter,
                    colorUtils,
                )
            }

            EVENT -> {
                val binding =
                    DataBindingUtil.inflate<ItemEventBinding>(
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

    override fun getItemViewType(position: Int): Int = getItem(position).type.ordinal

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        when (holder) {
            is EventViewHolder -> {
                getItem(position)?.let {
                    val materialView =
                        holder.itemView.findViewById<ConstraintLayout>(R.id.materialView)
                    materialView.visibility = View.GONE
                    val composeView = holder.itemView.findViewById<ComposeView>(R.id.composeView)
                    composeView.setContent {
                        val leftSpacing =
                            if (it.groupedByStage == true) Spacing.Spacing64 else Spacing.Spacing16
                        val bottomSpacing =
                            if (it.showBottomShadow) {
                                Spacing.Spacing16
                            } else {
                                Spacing.Spacing4
                            }
                        val card =
                            cardMapper.map(
                                event = it,
                                editable = it.editable,
                                displayOrgUnit = it.displayOrgUnit,
                                onCardClick = {
                                    val eventModel = it
                                    eventModel.event?.let { event ->
                                        when (event.status()) {
                                            EventStatus.SCHEDULE, EventStatus.OVERDUE -> {
                                                if (!eventModel.orgUnitIsInCaptureScope) {
                                                    presenter.onScheduleEventWithoutAccess(eventModel.orgUnitName)
                                                } else {
                                                    presenter.onScheduleSelected(
                                                        event.uid(),
                                                        composeView,
                                                    )
                                                }
                                            }

                                            else -> {
                                                presenter.onEventSelected(
                                                    event.uid(),
                                                    event.status()!!,
                                                )

                                                if (isLandscape()) {
                                                    if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                                                        currentList[previousSelectedPosition].isClicked =
                                                            false
                                                        notifyItemChanged(previousSelectedPosition)
                                                    }
                                                    previousSelectedPosition = position
                                                    getItem(position).isClicked = true
                                                    notifyItemChanged(position)
                                                }
                                            }
                                        }
                                    }
                                },
                            )

                        if (it.event?.uid() == initialSelectedEventUid && previousSelectedPosition == RecyclerView.NO_POSITION) {
                            it.isClicked = true
                            previousSelectedPosition = position
                        }
                        Box(
                            modifier =
                                Modifier
                                    .padding(
                                        start = leftSpacing,
                                        end = Spacing.Spacing16,
                                        bottom = bottomSpacing,
                                    ),
                        ) {
                            ListCardProvider(
                                card = card,
                                title =
                                    ListCardTitleModel(
                                        text = card.title,
                                        style =
                                            LocalTextStyle.current.copy(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight(500),
                                                lineHeight = 20.sp,
                                            ),
                                        color = TextColor.OnSurface,
                                    ),
                                syncingResourceId = R.string.syncing,
                            )

                            if (it.isClicked) {
                                Box(
                                    modifier =
                                        Modifier
                                            .matchParentSize()
                                            .background(
                                                color = SurfaceColor.Primary.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(Radius.S),
                                            ),
                                )
                            }
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

    override fun getItemId(position: Int): Long = getItem(position).hashCode().toLong()
}
